import time

import httpx
import respx

from hylian_client.sdk.hylian_client import HylianClient
from hylian_client.sdk.session.manager import SessionManager
from hylian_client.sdk.session.store import InMemorySessionStore
from hylian_client.sdk.shield import HylianShield, ShelterAction

from conftest import USER_JSON, err, ok


def build(config):
    store = InMemorySessionStore()
    client = HylianClient(config)
    manager = SessionManager(store, client, idle_seconds=1800)
    shield = HylianShield(client, manager, config)
    return shield, store, manager, client


def shelter(shield, session, *, path="/app/page", url="https://app.test/app/page",
            query=None, bearer=None):
    return shield.shelter(
        path=path,
        request_url=url,
        query_params=query or {},
        bearer_token=bearer,
        session=session,
    )


@respx.mock
def test_logout_redirects_and_invalidates(config):
    shield, store, manager, _ = build(config)
    session = store.create()
    session.is_token_session = True
    store.save(session)
    respx.post("https://sso.test/api/security/removeActivity").mock(
        return_value=httpx.Response(200, json=ok(True))
    )
    r = shelter(shield, session, path="/api/logout")
    assert r.action is ShelterAction.REDIRECT
    assert r.status_code == 302
    assert r.location.startswith("https://sso.test/api/security/logout?")
    assert store.get(session.sid) is None  # 会话被失效


@respx.mock
def test_sweep_invalidates_target_session(config):
    shield, store, manager, _ = build(config)
    victim = store.create()
    caller = store.create()
    r = shelter(shield, caller, path="/api/sweep",
                query={"session_id": victim.sid})
    assert r.action is ShelterAction.HANDLED
    assert store.get(victim.sid) is None


@respx.mock
def test_bearer_valid_allows(config, base_url):
    shield, store, _, _ = build(config)
    route = respx.get(f"{base_url}/getUser").mock(
        return_value=httpx.Response(200, json=ok(USER_JSON))
    )
    session = store.create()
    r = shelter(shield, session, bearer="good")
    assert r.action is ShelterAction.ALLOW
    assert session.user.name == "Alice"
    assert route.called


@respx.mock
def test_bearer_invalid_unauthorized(config, base_url):
    shield, store, _, _ = build(config)
    respx.get(f"{base_url}/getUser").mock(
        return_value=httpx.Response(200, json=err())
    )
    session = store.create()
    r = shelter(shield, session, bearer="bad")
    assert r.action is ShelterAction.UNAUTHORIZED


@respx.mock
def test_session_cache_hit_no_server_call(config, base_url):
    """会话已缓存 user 且 token 刚刷新 → 直接放行，不再调服务端。"""
    shield, store, _, _ = build(config)
    getuser = respx.get(f"{base_url}/getUser").mock(
        return_value=httpx.Response(200, json=ok(USER_JSON))
    )
    refresh = respx.post(f"{base_url}/refreshToken").mock(
        return_value=httpx.Response(200, json=ok("newtok"))
    )
    session = store.create()
    session.set_token("tok")  # token_refresh_time = now（未超间隔）
    session.user = object()
    r = shelter(shield, session)
    assert r.action is ShelterAction.ALLOW
    assert not getuser.called   # user 命中缓存
    assert not refresh.called   # token 未到刷新间隔


@respx.mock
def test_session_token_refresh_when_stale(config, base_url):
    shield, store, _, _ = build(config)
    respx.get(f"{base_url}/getUser").mock(
        return_value=httpx.Response(200, json=ok(USER_JSON))
    )
    refresh = respx.post(f"{base_url}/refreshToken").mock(
        return_value=httpx.Response(200, json=ok("newtok"))
    )
    session = store.create()
    session.set_token("old")
    session.user = object()
    # 令刷新时间戳超过间隔（config 默认 60s）
    session.token_refresh_time = int(time.time() * 1000) - 61_000
    r = shelter(shield, session)
    assert r.action is ShelterAction.ALLOW
    assert refresh.called
    assert session.token == "newtok"


@respx.mock
def test_no_token_no_code_redirects_to_apply_code(config):
    shield, store, _, _ = build(config)
    session = store.create()
    r = shelter(shield, session, url="https://app.test/app/page?x=1")
    assert r.action is ShelterAction.REDIRECT
    assert r.status_code == 303
    assert r.location.startswith("https://sso.test/api/security/applyCode?")
    assert "redirect_url=" in r.location


@respx.mock
def test_code_exchange_success(config, base_url):
    shield, store, _, _ = build(config)
    acquire = respx.get(f"{base_url}/acquireToken").mock(
        return_value=httpx.Response(200, json=ok("freshtoken"))
    )
    session = store.create()
    r = shelter(
        shield, session,
        url="https://app.test/app/page?code=abc&x=1",
        query={"code": "abc", "x": "1"},
    )
    assert r.action is ShelterAction.REDIRECT
    assert r.status_code == 303
    # 干净 URL 已剔除 code，保留其它参数
    assert "code=" not in r.location
    assert "x=1" in r.location
    assert acquire.called
    assert session.token == "freshtoken"
    assert session.is_token_session is True


@respx.mock
def test_code_exchange_failure_unauthorized(config, base_url):
    shield, store, _, _ = build(config)
    respx.get(f"{base_url}/acquireToken").mock(
        return_value=httpx.Response(200, json=err())
    )
    session = store.create()
    r = shelter(
        shield, session,
        url="https://app.test/app/page?code=bad",
        query={"code": "bad"},
    )
    assert r.action is ShelterAction.UNAUTHORIZED
