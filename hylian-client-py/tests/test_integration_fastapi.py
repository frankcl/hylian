import httpx
import pytest
import respx
from fastapi import Depends, FastAPI
from fastapi.testclient import TestClient

from hylian_client.sdk import context
from hylian_client.sdk.fastapi import acl, enable_hylian_guard

from conftest import USER_JSON, err, ok


def build_app(config):
    app = FastAPI()
    enable_hylian_guard(app, config)

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
    return build_app(config)


@pytest.fixture
def base_url():
    return "https://sso.test/api/security"


@respx.mock
def test_guarded_path_requires_token(app):
    with TestClient(app) as c:
        assert c.get("/me").status_code == 401


@respx.mock
def test_valid_token_injects_user(app, base_url):
    respx.get(f"{base_url}/getUser").mock(
        return_value=httpx.Response(200, json=ok(USER_JSON))
    )
    with TestClient(app) as c:
        r = c.get("/me", headers={"Authorization": "Bearer good"})
        assert r.status_code == 200
        assert r.json() == {"name": "Alice"}


@respx.mock
def test_invalid_token_401(app, base_url):
    respx.get(f"{base_url}/getUser").mock(
        return_value=httpx.Response(200, json=err())
    )
    with TestClient(app) as c:
        r = c.get("/me", headers={"Authorization": "Bearer bad"})
        assert r.status_code == 401


@respx.mock
def test_excluded_path_skips_auth(config):
    config.exclude_patterns = ["/health"]
    app = build_app(config)
    with TestClient(app) as c:
        assert c.get("/health").status_code == 200


@respx.mock
def test_acl_allow(app, base_url):
    respx.get(f"{base_url}/getUser").mock(
        return_value=httpx.Response(200, json=ok(USER_JSON))
    )
    respx.get(f"{base_url}/getAppUserRoles").mock(
        return_value=httpx.Response(200, json=ok([{"id": "r1"}]))
    )
    respx.post(f"{base_url}/getAppRolePermissions").mock(
        return_value=httpx.Response(200, json=ok([{"id": "p1", "path": "/orders"}]))
    )
    with TestClient(app) as c:
        r = c.get("/orders", headers={"Authorization": "Bearer good"})
        assert r.status_code == 200


@respx.mock
def test_acl_forbidden(app, base_url):
    respx.get(f"{base_url}/getUser").mock(
        return_value=httpx.Response(200, json=ok(USER_JSON))
    )
    respx.get(f"{base_url}/getAppUserRoles").mock(
        return_value=httpx.Response(200, json=ok([{"id": "r1"}]))
    )
    respx.post(f"{base_url}/getAppRolePermissions").mock(
        return_value=httpx.Response(200, json=ok([{"id": "p1", "path": "/users/**"}]))
    )
    with TestClient(app) as c:
        r = c.get("/orders", headers={"Authorization": "Bearer good"})
        assert r.status_code == 403


@respx.mock
def test_logout_redirects_to_server(app):
    with TestClient(app) as c:
        r = c.get("/api/logout", follow_redirects=False)
        assert r.status_code == 302
        loc = r.headers["location"]
        assert loc.startswith("https://sso.test/api/security/logout?")
        assert "app_id=app" in loc
        assert "app_secret=secret" in loc


@respx.mock
def test_logout_intercepted_without_token(app):
    # 即使未携带 token 也应拦截 logout（不返回 401）
    with TestClient(app) as c:
        r = c.get("/api/logout", follow_redirects=False)
        assert r.status_code == 302


@respx.mock
def test_cors_preflight(app):
    with TestClient(app) as c:
        r = c.options(
            "/me",
            headers={
                "Origin": "https://front.test",
                "Access-Control-Request-Method": "GET",
            },
        )
        assert r.status_code in (200, 204)
        assert r.headers["access-control-allow-origin"] == "https://front.test"
        assert r.headers["access-control-allow-credentials"] == "true"
