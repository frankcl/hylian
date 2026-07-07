"""ACL 访问控制依赖：对应 Java ``ACLAspect`` / ``@EnableACLAspect``。

逐路由 opt-in。校验逻辑与 Java 一致：取当前用户的权限列表，判断是否存在某条
``permission.path`` 模式能覆盖目标资源路径；不存在则 403。

用法::

    @router.get("/orders")
    async def list_orders(_: None = Depends(acl())):
        ...

    # 也可显式指定资源路径（默认用请求实际路径）
    @router.get("/orders")
    async def list_orders(_: None = Depends(acl("/orders/**"))):
        ...
"""

from __future__ import annotations

from collections.abc import Awaitable, Callable

from fastapi import HTTPException, Request
from starlette.concurrency import run_in_threadpool

from .. import context, permission
from ..hylian_client import HylianClient


def acl(resource: str | None = None) -> Callable[[Request], Awaitable[None]]:
    """构造一个 ACL 依赖；``resource`` 为空时用请求实际路径做匹配。"""

    async def dependency(request: Request) -> None:
        user = context.get_user()
        if user is None:
            raise HTTPException(status_code=401, detail="用户尚未登录")

        client: HylianClient = request.app.state.hylian_client
        # cookie/session 模式下优先用会话缓存的权限，避免每次鉴权都打服务端；
        # 纯 Bearer 模式无会话，退化为每次拉取（对齐 Java ACLAspect 行为）。
        session = getattr(request.state, "hylian_session", None)
        if session is not None and session.permissions is not None:
            permissions = session.permissions
        else:
            permissions = await run_in_threadpool(client.get_user_permissions, user)
            if session is not None:
                session.permissions = permissions
        target = resource or request.url.path
        if not permissions or not any(permission.match(p.path, target) for p in permissions):
            raise HTTPException(status_code=403, detail="无权操作")

    return dependency
