"""HylianGuard 中间件：对应 Java ``@EnableHylianGuard`` 的 Servlet Filter。

token 认证模式：引入后自动守卫全部路由（exclude 命中的放行）。
- 守卫路径：必须带合法 ``Authorization: Bearer <token>``，否则 401；
  校验通过则把当前用户注入 :mod:`sdk.context`。
- 放行路径：不强制鉴权；若带合法 token 则尽力填充用户上下文（best-effort）。

登录/登出不在此实现，由 hylian 产品负责；token 失效时返回 401，由前端跳转 hylian 登录。
"""

from __future__ import annotations

from starlette.concurrency import run_in_threadpool
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import JSONResponse, RedirectResponse, Response
from starlette.types import ASGIApp

from .. import constants as C
from .. import context
from ..hylian_client import HylianClient
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


class HylianGuardMiddleware(BaseHTTPMiddleware):
    def __init__(
        self,
        app: ASGIApp,
        client: HylianClient,
        exclude_patterns: list[URLPattern] | None = None,
        logout_path: str = C.CLIENT_PATH_LOGOUT,
    ) -> None:
        super().__init__(app)
        self.client = client
        self.exclude_patterns = exclude_patterns or []
        self.logout_path = logout_path

    async def dispatch(self, request: Request, call_next) -> Response:
        path = request.url.path

        # 拦截 logout：302 重定向浏览器到服务端 logout，由 hylian 完成全局注销。
        # 对齐 Java HylianShield 优先拦截 /api/logout 的行为。
        if path == self.logout_path:
            return RedirectResponse(self.client.logout_redirect_url(), status_code=302)

        token = _bearer_token(request)
        excluded = match_exclude(path, self.exclude_patterns)

        if not excluded and not token:
            return _unauthorized("缺少 Bearer Token")

        user = None
        if token:
            user = await run_in_threadpool(self.client.get_user, token)
            if user is None and not excluded:
                return _unauthorized("Token 验证失败")

        ctx_token = context.set_user(user)
        try:
            return await call_next(request)
        finally:
            context.reset_user(ctx_token)
