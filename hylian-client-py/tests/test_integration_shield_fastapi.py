import httpx
import pytest
import respx
from fastapi import Depends, FastAPI
from fastapi.testclient import TestClient

from hylian_client.sdk import context
from hylian_client.sdk.fastapi import acl, enable_hylian_shield

from conftest import USER_JSON, ok


def build_app(config):
    app = FastAPI()
    enable_hylian_shield(app, config)

    @app.get("/health")
    def health():
        return {"ok": True}

    @app.get("/me")
    def me():
        user = context.get_user()
        return {"name": user.name if user else None}

    @app.get("/orders")
    def orders(_: None = Depends(acl())):
        return {"orders": []}

    return app


@pytest.fixture
def app(config):
    # cookie 走非 HTTPS 的 TestClient，需要关闭 Secure 才能被回传
    config.session_cookie_secure = False
    return build_app(config)


@pytest.fixture
def base_url():
    return "https://sso.test/api/security"


@respx.mock
def test_unauthenticated_redirects_to_apply_code(app):
    with TestClient(app) as c:
        r = c.get("/me", follow_redirects=False)
        assert r.status_code == 303
        assert r.headers["location"].startswith(
            "https://sso.test/api/security/applyCode?"
        )
        # 首次访问下发会话 cookie，使回调命中同一会话
        assert "HYLIAN_SESSION" in r.cookies


@respx.mock
def test_code_callback_then_cache_hit(app, base_url):
    """?code= 回调换 token 并下发会话 cookie；带 cookie 再次访问命中缓存，不再打服务端。"""
    acquire = respx.get(f"{base_url}/acquireToken").mock(
        return_value=httpx.Response(200, json=ok("tok"))
    )
    getuser = respx.get(f"{base_url}/getUser").mock(
        return_value=httpx.Response(200, json=ok(USER_JSON))
    )
    respx.post(f"{base_url}/refreshToken").mock(
        return_value=httpx.Response(200, json=ok("tok2"))
    )
    with TestClient(app) as c:
        # 1) 首次访问 → 303 到 applyCode，拿到会话 cookie
        r0 = c.get("/me", follow_redirects=False)
        assert r0.status_code == 303

        # 2) 模拟 SSO 回调带 code：换 token 后 303 回干净 URL
        r1 = c.get("/me?code=abc", follow_redirects=False)
        assert r1.status_code == 303
        assert "code=" not in r1.headers["location"]
        assert acquire.called

        # 3) 干净 URL 再次访问：会话已有 token，getUser 拉一次用户并缓存
        r2 = c.get("/me")
        assert r2.status_code == 200
        assert r2.json() == {"name": "Alice"}
        assert getuser.call_count == 1

        # 4) 再次访问：user 命中会话缓存，getUser 不再被调用
        r3 = c.get("/me")
        assert r3.status_code == 200
        assert getuser.call_count == 1  # 未增加 → 缓存生效


@respx.mock
def test_bearer_mode_takes_precedence(app, base_url):
    """带 Bearer 头时走 API 模式，直接校验 token。"""
    respx.get(f"{base_url}/getUser").mock(
        return_value=httpx.Response(200, json=ok(USER_JSON))
    )
    with TestClient(app) as c:
        r = c.get("/me", headers={"Authorization": "Bearer good"})
        assert r.status_code == 200
        assert r.json() == {"name": "Alice"}


@respx.mock
def test_excluded_path_skips_auth(config):
    config.session_cookie_secure = False
    config.exclude_patterns = ["/health"]
    app = build_app(config)
    with TestClient(app) as c:
        assert c.get("/health").status_code == 200


@respx.mock
def test_logout_redirects_and_sweep(app, base_url):
    respx.get(f"{base_url}/acquireToken").mock(
        return_value=httpx.Response(200, json=ok("tok"))
    )
    respx.get(f"{base_url}/getUser").mock(
        return_value=httpx.Response(200, json=ok(USER_JSON))
    )
    respx.post(f"{base_url}/refreshToken").mock(
        return_value=httpx.Response(200, json=ok("tok2"))
    )
    remove_activity = respx.post(f"{base_url}/removeActivity").mock(
        return_value=httpx.Response(200, json=ok(True))
    )
    with TestClient(app) as c:
        c.get("/me?code=abc", follow_redirects=False)  # 建立 token 会话
        c.get("/me")  # 填充缓存
        r = c.get("/api/logout", follow_redirects=False)
        assert r.status_code == 302
        assert r.headers["location"].startswith(
            "https://sso.test/api/security/logout?"
        )
        # token 会话销毁时通知服务端 removeActivity
        assert remove_activity.called


@respx.mock
def test_sweep_endpoint_invalidates(app, base_url):
    respx.get(f"{base_url}/acquireToken").mock(
        return_value=httpx.Response(200, json=ok("tok"))
    )
    respx.get(f"{base_url}/getUser").mock(
        return_value=httpx.Response(200, json=ok(USER_JSON))
    )
    respx.post(f"{base_url}/refreshToken").mock(
        return_value=httpx.Response(200, json=ok("tok2"))
    )
    respx.post(f"{base_url}/removeActivity").mock(
        return_value=httpx.Response(200, json=ok(True))
    )
    manager = app.state.hylian_session_manager
    with TestClient(app) as c:
        c.get("/me?code=abc", follow_redirects=False)
        sid = list(c.cookies.values())[0] if c.cookies else None
        # 找到已建立的 token 会话
        sids = [s for s in manager.store.all_ids() if manager.store.get(s).is_token_session]
        assert sids
        target = sids[0]
        r = c.get(f"/api/sweep?session_id={target}")
        assert r.status_code == 200
        assert manager.store.get(target) is None
