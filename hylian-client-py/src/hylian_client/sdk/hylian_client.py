"""HylianClient：到 Java hylian-server 的 HTTP 客户端（token 认证模式）。

对齐 Java ``core.HylianClient`` 中 token 模式相关方法，剔除 cookie/session/login/logout。
所有响应都被 :class:`WebResponse` 信封包裹，``status`` 为假时返回 ``None``/空列表。
"""

from __future__ import annotations

from typing import Any
from urllib.parse import urlencode

import httpx

from . import constants as C
from .config import HylianClientConfig
from .models import Permission, Role, User
from .web_response import WebResponse


class HylianClient:
    def __init__(self, config: HylianClientConfig, client: httpx.Client | None = None) -> None:
        config.check()
        self.config = config
        self._client = client or httpx.Client(
            verify=config.verify_tls,
            timeout=config.timeout,
        )

    # ---- 底层请求 ----

    def _url(self, path: str) -> str:
        return f"{self.config.server_url}{path}"

    def _app_params(self) -> dict[str, Any]:
        return {
            C.PARAM_APP_ID: self.config.app_id,
            C.PARAM_APP_SECRET: self.config.app_secret,
        }

    def _unwrap(self, resp: httpx.Response) -> Any:
        # 服务端对「无结果」的接口（如无效 token 的 getUser）会返回 204/空 body，
        # 直接 resp.json() 会抛 JSONDecodeError——空响应一律视为无数据。
        if resp.status_code == 204 or not resp.content:
            return None
        envelope = WebResponse.parse(resp.json())
        return envelope.data if envelope.status else None

    def _get(self, path: str, params: dict[str, Any]) -> Any:
        resp = self._client.get(self._url(path), params=params)
        return self._unwrap(resp)

    def _post(self, path: str, body: dict[str, Any]) -> Any:
        resp = self._client.post(self._url(path), json=body)
        return self._unwrap(resp)

    def logout_redirect_url(self) -> str:
        """服务端 logout 的重定向地址（对齐 Java ``forceLogout``）。

        浏览器被重定向到这里，带着 SSO 域上的 TICKET cookie 命中服务端 logout，
        由服务端完成全局注销并清除 TICKET/TOKEN cookie。
        """
        query = urlencode(self._app_params())
        return f"{self._url(C.SERVER_PATH_LOGOUT)}?{query}"

    def apply_code_url(self, redirect_url: str) -> str:
        """未登录时重定向到服务端 ``applyCode`` 的地址（对齐 Java ``HylianShield`` 的重定向）。

        浏览器带着 SSO 域上的 TICKET cookie 命中 ``applyCode``，服务端签发一次性 code
        并重定向回 ``redirect_url?code=<code>``，客户端再用 code 换 token。
        """
        query = urlencode({**self._app_params(), C.PARAM_REDIRECT_URL: redirect_url})
        return f"{self._url(C.SERVER_PATH_APPLY_CODE)}?{query}"

    # ---- 业务接口 ----

    def acquire_token(self, code: str, session_id: str) -> str | None:
        """用一次性 code 向服务端换取 token（对齐 Java ``acquireToken``）。

        ``session_id`` 让服务端把本地会话与其"应用登录活动(activity)"绑定，
        以便会话销毁时通过 ``remove_activity`` 注销。校验失败返回 ``None``。
        """
        params = {
            C.PARAM_CODE: code,
            C.PARAM_SESSION_ID: session_id,
            **self._app_params(),
        }
        data = self._get(C.SERVER_PATH_ACQUIRE_TOKEN, params)
        return data if data is not None else None

    def remove_activity(self, session_id: str) -> bool:
        """注销会话对应的应用登录活动（对齐 Java ``removeActivity``）。"""
        body = {C.PARAM_SESSION_ID: session_id, **self._app_params()}
        data = self._post(C.SERVER_PATH_REMOVE_ACTIVITY, body)
        return bool(data)

    def get_user(self, token: str) -> User | None:
        """用 token 向服务端换取用户信息，校验失败返回 ``None``。"""
        params = {C.PARAM_TOKEN: token, **self._app_params()}
        data = self._get(C.SERVER_PATH_GET_USER, params)
        return User.model_validate(data) if data is not None else None

    def refresh_token(self, token: str) -> str | None:
        """用旧 token 换取新 token，校验失败返回 ``None``（对齐 Java ``refreshToken``）。"""
        body = {C.PARAM_TOKEN: token, **self._app_params()}
        data = self._post(C.SERVER_PATH_REFRESH_TOKEN, body)
        return data if data is not None else None

    def get_user_by_id(self, user_id: str) -> User | None:
        body = {C.PARAM_USER_ID: user_id, **self._app_params()}
        data = self._post(C.SERVER_PATH_GET_USER_BY_ID, body)
        return User.model_validate(data) if data is not None else None

    def get_users(self) -> list[User]:
        data = self._post(C.SERVER_PATH_GET_USERS, dict(self._app_params()))
        return [User.model_validate(u) for u in (data or [])]

    def batch_get_users(self, user_ids: list[str]) -> list[User]:
        body = {C.PARAM_USER_IDS: user_ids, **self._app_params()}
        data = self._post(C.SERVER_PATH_BATCH_GET_USERS, body)
        return [User.model_validate(u) for u in (data or [])]

    def get_user_roles(self, user: User) -> list[Role]:
        params = {C.PARAM_USER_ID: user.id, **self._app_params()}
        data = self._get(C.SERVER_PATH_GET_APP_USER_ROLES, params)
        return [Role.model_validate(r) for r in (data or [])]

    def get_role_permissions(self, role_ids: list[str]) -> list[Permission]:
        body = {C.PARAM_ROLE_IDS: role_ids, **self._app_params()}
        data = self._post(C.SERVER_PATH_GET_APP_ROLE_PERMISSIONS, body)
        return [Permission.model_validate(p) for p in (data or [])]

    def get_user_permissions(self, user: User) -> list[Permission]:
        roles = self.get_user_roles(user)
        if not roles:
            return []
        role_ids = [r.id for r in roles if r.id]
        if not role_ids:
            return []
        return self.get_role_permissions(role_ids)

    def is_app_admin(self, user: User) -> bool:
        params = {C.PARAM_USER_ID: user.id, **self._app_params()}
        data = self._get(C.SERVER_PATH_IS_APP_ADMIN, params)
        return bool(data)

    def close(self) -> None:
        self._client.close()
