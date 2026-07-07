"""会话数据与存储，对齐 Java ``HttpSession`` 内容（``SessionConstants``）与 ``SessionManager.sessionMap``。

``SessionData`` 一条会话把 token/user 等缓存在服务端，避免每请求都往返 hylian-server。
``SessionStore`` 抽象出存储接口，默认 ``InMemorySessionStore``（进程内 dict），
接口为以后接 Redis（多 worker 共享）预留。
"""

from __future__ import annotations

import secrets
import threading
import time
from abc import ABC, abstractmethod
from dataclasses import dataclass, field

from ..models import Permission, Role, User


def _now_ms() -> int:
    return int(time.time() * 1000)


@dataclass
class SessionData:
    """一条服务端会话，字段对齐 Java ``SessionConstants``。

    token/user/roles/permissions 缓存于此，是"避免频繁打 server"的落点。
    ``lock`` 不参与序列化，仅用于进程内并发保护。
    """

    sid: str
    token: str | None = None
    # token 最近一次刷新/设置的毫秒时间戳（对齐 __TOKEN_REFRESH_TIME__）
    token_refresh_time: int | None = None
    user: User | None = None
    roles: list[Role] | None = None
    permissions: list[Permission] | None = None
    # 强制下次重取 user 的标志（对齐 __REFRESH_USER__）
    refresh_user: bool = False
    # 标记该会话已换过 token/登录（对齐 SessionManager.tokenSessions）
    is_token_session: bool = False
    # 最后访问毫秒时间戳，供空闲超时回收
    last_access: int = field(default_factory=_now_ms)
    # 会话级锁（对齐 __LOCK__ 的 ReentrantLock），不序列化
    lock: threading.Lock = field(default_factory=threading.Lock, repr=False, compare=False)

    def set_token(self, token: str) -> None:
        """设置 token 并打刷新时间戳（对齐 ``SessionUtils.setToken``）。"""
        self.token = token
        self.token_refresh_time = _now_ms()

    def clear_resources(self) -> None:
        """清空鉴权资源，保留 lock（对齐 ``SessionUtils.removeResources``）。"""
        self.token = None
        self.token_refresh_time = None
        self.user = None
        self.roles = None
        self.permissions = None
        self.refresh_user = False


class SessionStore(ABC):
    """会话存储接口。实现须保证并发安全。"""

    @abstractmethod
    def create(self) -> SessionData:
        """创建带唯一 sid 的空会话并落库。"""

    @abstractmethod
    def get(self, sid: str) -> SessionData | None:
        """按 sid 取会话，不存在返回 ``None``。"""

    @abstractmethod
    def save(self, data: SessionData) -> None:
        """持久化会话改动（内存实现下 get 返回的即本体，此处刷新时间戳/落库幂等）。"""

    @abstractmethod
    def remove(self, sid: str) -> None:
        """删除会话。"""

    @abstractmethod
    def all_ids(self) -> list[str]:
        """所有会话 sid 快照，供回收线程扫描。"""


class InMemorySessionStore(SessionStore):
    """进程内内存存储，对齐 Java ``SessionManager.sessionMap``。仅单进程有效。"""

    def __init__(self) -> None:
        self._sessions: dict[str, SessionData] = {}
        self._lock = threading.Lock()

    def create(self) -> SessionData:
        with self._lock:
            while True:
                sid = secrets.token_urlsafe(32)
                if sid not in self._sessions:
                    break
            data = SessionData(sid=sid)
            self._sessions[sid] = data
            return data

    def get(self, sid: str) -> SessionData | None:
        with self._lock:
            return self._sessions.get(sid)

    def save(self, data: SessionData) -> None:
        with self._lock:
            self._sessions[data.sid] = data

    def remove(self, sid: str) -> None:
        with self._lock:
            self._sessions.pop(sid, None)

    def all_ids(self) -> list[str]:
        with self._lock:
            return list(self._sessions.keys())
