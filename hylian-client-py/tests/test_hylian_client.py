import httpx
import pytest
import respx

from hylian_client.sdk.hylian_client import HylianClient

from conftest import USER_JSON, err, ok


@pytest.fixture
def client(config):
    return HylianClient(config)


@respx.mock
def test_get_user_ok(client, base_url):
    route = respx.get(f"{base_url}/getUser").mock(
        return_value=httpx.Response(200, json=ok(USER_JSON))
    )
    user = client.get_user("tok")
    assert user is not None
    assert user.id == "u1" and user.username == "alice"
    assert user.wx_openid == "wx123"
    # 校验透传参数
    req = route.calls.last.request
    assert "token=tok" in str(req.url)
    assert "app_id=app" in str(req.url)
    assert "app_secret=secret" in str(req.url)


@respx.mock
def test_get_user_invalid_token(client, base_url):
    respx.get(f"{base_url}/getUser").mock(
        return_value=httpx.Response(200, json=err())
    )
    assert client.get_user("bad") is None


@respx.mock
def test_refresh_token_ok(client, base_url):
    route = respx.post(f"{base_url}/refreshToken").mock(
        return_value=httpx.Response(200, json=ok("new-token"))
    )
    assert client.refresh_token("old") == "new-token"
    # 校验请求体透传
    import json as _json

    body = _json.loads(route.calls.last.request.content)
    assert body["token"] == "old"
    assert body["app_id"] == "app"
    assert body["app_secret"] == "secret"


@respx.mock
def test_refresh_token_invalid(client, base_url):
    respx.post(f"{base_url}/refreshToken").mock(
        return_value=httpx.Response(200, json=err())
    )
    assert client.refresh_token("bad") is None


@respx.mock
def test_get_user_by_id(client, base_url):
    respx.post(f"{base_url}/getUserById").mock(
        return_value=httpx.Response(200, json=ok(USER_JSON))
    )
    user = client.get_user_by_id("u1")
    assert user.id == "u1"


@respx.mock
def test_get_users(client, base_url):
    respx.post(f"{base_url}/getUsers").mock(
        return_value=httpx.Response(200, json=ok([USER_JSON, USER_JSON]))
    )
    assert len(client.get_users()) == 2


@respx.mock
def test_get_users_empty_on_error(client, base_url):
    respx.post(f"{base_url}/getUsers").mock(
        return_value=httpx.Response(200, json=err())
    )
    assert client.get_users() == []


@respx.mock
def test_user_permissions(client, base_url):
    respx.get(f"{base_url}/getAppUserRoles").mock(
        return_value=httpx.Response(200, json=ok([{"id": "r1", "name": "admin"}]))
    )
    perm_route = respx.post(f"{base_url}/getAppRolePermissions").mock(
        return_value=httpx.Response(200, json=ok([{"id": "p1", "path": "/orders/**"}]))
    )
    from hylian_client.sdk.models import User

    perms = client.get_user_permissions(User.model_validate(USER_JSON))
    assert [p.path for p in perms] == ["/orders/**"]
    # role_ids 正确透传
    import json as _json

    body = _json.loads(perm_route.calls.last.request.content)
    assert body["role_ids"] == ["r1"]


@respx.mock
def test_user_permissions_no_roles(client, base_url):
    respx.get(f"{base_url}/getAppUserRoles").mock(
        return_value=httpx.Response(200, json=ok([]))
    )
    from hylian_client.sdk.models import User

    assert client.get_user_permissions(User.model_validate(USER_JSON)) == []


@respx.mock
def test_is_app_admin(client, base_url):
    respx.get(f"{base_url}/isAppAdmin").mock(
        return_value=httpx.Response(200, json=ok(True))
    )
    from hylian_client.sdk.models import User

    assert client.is_app_admin(User.model_validate(USER_JSON)) is True
