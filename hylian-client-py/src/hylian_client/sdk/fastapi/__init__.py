"""FastAPI 集成层。

- :func:`enable_hylian_shield` —— **推荐**，完整 cookie/session 模式闭环：先 Bearer 后 session，
  token/user 缓存在服务端会话，减少与 hylian-server 的往返。对应 Java 完整 ``HylianShield``。
- :func:`enable_hylian_guard` —— 纯 Bearer/API 模式（无会话缓存），保留兼容旧调用。
"""

from __future__ import annotations

from fastapi import FastAPI

from ..config import HylianClientConfig
from ..hylian_client import HylianClient
from ..session.manager import SessionManager
from ..session.store import InMemorySessionStore, SessionStore
from ..shield import HylianShield
from ..url_pattern import build_exclude_patterns
from .acl import acl
from .cors import add_cors
from .guard import HylianGuardMiddleware
from .shield import HylianShieldMiddleware

__all__ = [
    "enable_hylian_shield",
    "enable_hylian_guard",
    "acl",
    "add_cors",
    "HylianShieldMiddleware",
    "HylianGuardMiddleware",
]


def enable_hylian_shield(
    app: FastAPI,
    config: HylianClientConfig | None = None,
    *,
    client: HylianClient | None = None,
    store: SessionStore | None = None,
    cors: bool = True,
    cors_origins: list[str] | None = None,
) -> HylianClient:
    """对应 Java 完整 ``@EnableHylianGuard``：挂上统一 shield 中间件（cookie/session 模式）。

    单一入口按优先级分流：先 ``Authorization: Bearer``（API 模式），没有则 session 模式
    （会话缓存 token+user、``?code=`` 换 token、``applyCode`` 重定向、token 定时刷新）。

    配置缺省从环境变量读取（``HYLIAN_*``）。返回 :class:`HylianClient`，并把 client、
    :class:`SessionManager` 挂到 ``app.state`` 供 ACL 依赖复用。默认内存会话存储；
    多 worker/多实例部署请传入共享的 ``store`` 实现（如 Redis）。
    """
    config = config or HylianClientConfig()
    config.check()
    client = client or HylianClient(config)
    store = store or InMemorySessionStore()
    manager = SessionManager(
        store,
        client,
        idle_seconds=config.session_idle_seconds,
    )
    shield = HylianShield(client, manager, config)

    app.state.hylian_client = client
    app.state.hylian_session_manager = manager

    manager.start_reaper()
    # 应用关闭时停回收线程；兼容不同 Starlette 版本的事件注册入口。
    # 回收线程本就是 daemon，注册失败也不影响进程退出。
    for target in (app, getattr(app, "router", None)):
        if target is not None and hasattr(target, "add_event_handler"):
            target.add_event_handler("shutdown", manager.stop_reaper)
            break

    # Starlette 中间件后加的在外层。先加 shield，再加 CORS，使 CORS 处于最外层，
    # 预检 OPTIONS 由 CORS 直接应答（对齐 Java CORSFilter 先于 guard 短路）。
    app.add_middleware(
        HylianShieldMiddleware,
        shield=shield,
        config=config,
        exclude_patterns=build_exclude_patterns(config.exclude_patterns),
    )
    if cors:
        add_cors(app, cors_origins)
    return client


def enable_hylian_guard(
    app: FastAPI,
    config: HylianClientConfig | None = None,
    *,
    client: HylianClient | None = None,
    cors: bool = True,
    cors_origins: list[str] | None = None,
) -> HylianClient:
    """纯 Bearer/API 模式（无会话缓存），对应 Java ``HylianShield`` 的 API 分支。

    需要完整 SSO 登录闭环与 token/user 会话缓存时改用 :func:`enable_hylian_shield`。
    配置缺省从环境变量读取（``HYLIAN_*``）。返回 :class:`HylianClient`，
    同时挂在 ``app.state.hylian_client`` 供 ACL 依赖使用。
    """
    config = config or HylianClientConfig()
    config.check()
    client = client or HylianClient(config)
    app.state.hylian_client = client

    # Starlette 中间件后加的在外层。先加 guard，再加 CORS，使 CORS 处于最外层，
    # 这样预检 OPTIONS 会被 CORS 直接应答（对齐 Java CORSFilter 先于 guard 短路）。
    app.add_middleware(
        HylianGuardMiddleware,
        client=client,
        exclude_patterns=build_exclude_patterns(config.exclude_patterns),
        logout_path=config.logout_path,
    )
    if cors:
        add_cors(app, cors_origins)
    return client
