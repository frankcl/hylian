"""HylianShield 中间件（cookie/session 模式）：对应 Java ``@EnableHylianGuard`` 的完整
``HylianShield`` 闭环。

单一统一入口，按优先级分流：**先看 ``Authorization: Bearer``，有则 API 模式；没有才走
session/cookie 模式**（会话缓存 token+user、``?code=`` 换 token、``applyCode`` 重定向、
token 定时刷新）。会话数据存服务端，浏览器仅持有 httpOnly 的 sid cookie。
"""

from __future__ import annotations

from starlette.concurrency import run_in_threadpool
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import JSONResponse, RedirectResponse, Response
from starlette.types import ASGIApp

from .. import constants as C
from .. import context
from ..config import HylianClientConfig
from ..shield import HylianShield, ShelterAction
from ..url_pattern import URLPattern, match_exclude


def _bearer_token(request: Request) -> str | None:
    """对齐 Java ``HTTPUtils.getTokenFromHeader``：必须是 ``Bearer <token>``。"""
    value = request.headers.get(C.HEADER_AUTHORIZATION)
    prefix = f"{C.PREFIX_BEARER} "
    if not value or not value.startswith(prefix):
        return None
    return value[len(prefix):]


def _unauthorized(message: str) -> JSONResponse:
    return JSONResponse(
        status_code=401,
        content={"status": False, "code": 401, "message": message},
    )


class HylianShieldMiddleware(BaseHTTPMiddleware):
    def __init__(
        self,
        app: ASGIApp,
        shield: HylianShield,
        config: HylianClientConfig,
        exclude_patterns: list[URLPattern] | None = None,
    ) -> None:
        super().__init__(app)
        self.shield = shield
        self.config = config
        self.manager = shield.manager
        self.exclude_patterns = exclude_patterns or []

    async def dispatch(self, request: Request, call_next) -> Response:
        path = request.url.path
        sid = request.cookies.get(self.config.session_cookie_name)
        session, is_new = self.manager.get_or_create(sid)
        # 供 acl() 依赖复用会话缓存的 roles/permissions
        request.state.hylian_session = session

        # exclude 命中：不强制鉴权，尽力用会话缓存回填用户上下文
        if match_exclude(path, self.exclude_patterns):
            ctx = context.set_user(session.user)
            try:
                response = await call_next(request)
            finally:
                context.reset_user(ctx)
            return self._with_cookie(response, session.sid, is_new)

        result = await run_in_threadpool(
            self.shield.shelter,
            path=path,
            request_url=str(request.url),
            query_params=dict(request.query_params),
            bearer_token=_bearer_token(request),
            session=session,
        )

        if result.action is ShelterAction.ALLOW:
            ctx = context.set_user(session.user)
            try:
                response = await call_next(request)
            finally:
                context.reset_user(ctx)
        elif result.action is ShelterAction.REDIRECT:
            assert result.location is not None
            response = RedirectResponse(result.location, status_code=result.status_code or 303)
        elif result.action is ShelterAction.UNAUTHORIZED:
            response = _unauthorized(result.message or "未授权")
        else:  # HANDLED
            response = Response(status_code=200)

        return self._with_cookie(response, session.sid, is_new)

    def _with_cookie(self, response: Response, sid: str, is_new: bool) -> Response:
        """新会话在响应上下发 httpOnly 的 sid cookie，使后续请求（含 applyCode 回调）命中同一会话。"""
        if is_new:
            response.set_cookie(
                self.config.session_cookie_name,
                sid,
                httponly=True,
                secure=self.config.session_cookie_secure,
                samesite=self.config.session_cookie_samesite,
                path="/",
            )
        return response
