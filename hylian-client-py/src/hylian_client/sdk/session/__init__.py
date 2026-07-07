"""服务端会话（cookie/session 模式）：对齐 Java ``HttpSession`` + ``SessionManager``。

Python/FastAPI 无 Servlet 容器自带的会话，故自建：httpOnly 的 sid cookie + 服务端存储。
"""

from __future__ import annotations

from .manager import SessionManager
from .store import InMemorySessionStore, SessionData, SessionStore

__all__ = [
    "SessionData",
    "SessionStore",
    "InMemorySessionStore",
    "SessionManager",
]
