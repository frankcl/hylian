import pytest

from hylian_client.sdk.config import HylianClientConfig


def ok(data):
    return {"status": True, "code": 200, "request_id": "t", "data": data}


def err(code=401, message="fail"):
    return {"status": False, "code": code, "message": message}


USER_JSON = {
    "id": "u1",
    "username": "alice",
    "name": "Alice",
    "tenant_id": "t1",
    "super_admin": False,
    "wx_openid": "wx123",
    "register_mode": 0,
}


@pytest.fixture
def config():
    return HylianClientConfig(
        app_id="app",
        app_secret="secret",
        server_url="https://sso.test/",
        verify_tls=False,
    )


@pytest.fixture
def base_url():
    return "https://sso.test/api/security"
