"""Hylian Python 客户端 SDK 顶层包。

对接 Java hylian-server：token 认证 + ACL + CORS + logout 拦截。
常用符号在此 re-export，FastAPI 集成在 :mod:`hylian_client.sdk.fastapi`。
"""

from __future__ import annotations

from .sdk import (
    HylianClient,
    HylianClientConfig,
    Permission,
    Role,
    Tenant,
    User,
    get_user,
)

__all__ = [
    "HylianClient",
    "HylianClientConfig",
    "User",
    "Role",
    "Permission",
    "Tenant",
    "get_user",
]
