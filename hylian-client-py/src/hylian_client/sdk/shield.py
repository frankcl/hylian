"""Hylian 盾：安全检测与登录认证核心，1:1 移植 Java ``core.HylianShield``。

框架无关：输入原语（路径/URL/查询参数/Bearer token/会话），输出一个 :class:`ShelterResult`
描述中间件该如何应答（放行/重定向/401/空 200）。判定优先级与 Java 一致：

1. ``/api/logout`` → 重定向服务端 logout 并失效本地会话；
2. ``/api/sweep`` → 按 ``session_id`` 远程失效本地会话；
3. ``Authorization: Bearer`` → API 模式，逐请求 ``get_user`` 校验，失败 401；
4. 会话内已有 token → cookie 模式，刷新 user（缓存）+ 刷新 token（定时），成功放行；
5. ``?code=`` → 用 code 换 token、写会话、重定向到去掉 code 的干净 URL；
6. 否则 → 303 重定向服务端 ``applyCode``。
"""

from __future__ import annotations

import logging
import time
from dataclasses import dataclass
from enum import Enum, auto
from urllib.parse import urlencode, urlparse, urlunparse

from . import constants as C
from .config import HylianClientConfig
from .hylian_client import HylianClient
from .session.manager import SessionManager
from .session.store import SessionData

logger = logging.getLogger(__name__)


class ShelterAction(Enum):
    ALLOW = auto()          # 放行，当前用户在 session.user
    REDIRECT = auto()       # 发重定向（location + status_code）
    UNAUTHORIZED = auto()   # 401
    HANDLED = auto()        # 已处理，返回空 200（sweep）


@dataclass
class ShelterResult:
    action: ShelterAction
    location: str | None = None
    status_code: int | None = None
    message: str | None = None

    @classmethod
    def allow(cls) -> "ShelterResult":
        return cls(ShelterAction.ALLOW)

    @classmethod
    def redirect(cls, location: str, status_code: int = 303) -> "ShelterResult":
        return cls(ShelterAction.REDIRECT, location=location, status_code=status_code)

    @classmethod
    def unauthorized(cls, message: str) -> "ShelterResult":
        return cls(ShelterAction.UNAUTHORIZED, message=message)

    @classmethod
    def handled(cls) -> "ShelterResult":
        return cls(ShelterAction.HANDLED)


def _now_ms() -> int:
    return int(time.time() * 1000)


def _remove_query_param(url: str, name: str) -> str:
    """从 URL 中剔除某个查询参数（对齐 Java ``HTTPUtils.removeQueries``）。"""
    parts = urlparse(url)
    pairs = [
        (k, v)
        for k, v in (
            p.split("=", 1) if "=" in p else (p, "")
            for p in parts.query.split("&")
            if p
        )
        if k != name
    ]
    new_query = urlencode(pairs)
    return urlunparse(parts._replace(query=new_query))


class HylianShield:
    def __init__(
        self,
        client: HylianClient,
        manager: SessionManager,
        config: HylianClientConfig,
    ) -> None:
        self.client = client
        self.manager = manager
        self.config = config
        self._refresh_interval_ms = config.token_refresh_interval_seconds * 1000

    def shelter(
        self,
        *,
        path: str,
        request_url: str,
        query_params: dict[str, str],
        bearer_token: str | None,
        session: SessionData,
    ) -> ShelterResult:
        # 1. 特殊路径优先
        if path == self.config.logout_path:
            self.manager.invalidate(session.sid)
            return ShelterResult.redirect(self.client.logout_redirect_url(), status_code=302)
        if path == self.config.sweep_path:
            self.manager.invalidate(query_params.get(C.PARAM_SESSION_ID))
            return ShelterResult.handled()

        # 2. Bearer 头 → API 模式（不落 token 会话）
        if bearer_token:
            user = self.client.get_user(bearer_token)
            if user is None:
                logger.error("Invalid bearer token")
                return ShelterResult.unauthorized("Token 验证失败")
            session.user = user
            session.refresh_user = False
            self.manager.touch(session)
            return ShelterResult.allow()

        # 3. 会话内已有 token → cookie 模式，刷新 user + token
        if session.token:
            if self._refresh_user(session) and self._refresh_token(session):
                self.manager.touch(session)
                return ShelterResult.allow()
            logger.warning("Session token is expired")
        session.clear_resources()
        self.manager.touch(session)

        # 4. ?code= 换 token
        code = query_params.get(C.PARAM_CODE)
        if not code:
            # 5. 否则重定向到 applyCode
            logger.info("Apply code for acquiring token")
            return ShelterResult.redirect(self.client.apply_code_url(request_url), status_code=303)

        clean_url = _remove_query_param(request_url, C.PARAM_CODE)
        with session.lock:
            if session.token:  # 双检：其他请求已完成换取
                return ShelterResult.redirect(clean_url, status_code=303)
            token = self.client.acquire_token(code, session.sid)
            if not token:
                logger.error("Acquire token failed")
                return ShelterResult.unauthorized("获取 Token 失败")
            logger.info("Acquire token success")
            self.manager.mark_token_session(session)
            session.set_token(token)
            self.manager.touch(session)
        return ShelterResult.redirect(clean_url, status_code=303)

    def _refresh_user(self, session: SessionData) -> bool:
        """无 user 或带强制刷新标志时才重取 user，否则用缓存（对齐 Java ``refreshUser``）。"""
        if not session.refresh_user and session.user is not None:
            return True
        user = self.client.get_user(session.token)  # type: ignore[arg-type]
        if user is None:
            logger.error("Get user failed for session %s", session.sid)
            return False
        session.user = user
        session.refresh_user = False
        return True

    def _refresh_token(self, session: SessionData) -> bool:
        """距上次刷新超过间隔才向服务端换新 token（对齐 Java ``refreshToken``）。"""
        token = session.token
        refresh_time = session.token_refresh_time
        interval = _now_ms() - refresh_time if refresh_time is not None else None
        if interval is not None and interval <= self._refresh_interval_ms:
            return True
        with session.lock:
            if token != session.token:  # 双检：其他请求已刷新（对齐 Java）
                return True
            new_token = self.client.refresh_token(token)  # type: ignore[arg-type]
            if not new_token:
                return False
            session.set_token(new_token)
            return True
