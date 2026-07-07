"""当前请求的用户上下文，用 ``ContextVar`` 替代 Java 的 ThreadLocal ``ContextManager``。"""

from __future__ import annotations

from contextvars import ContextVar

from .models import User

_current_user: ContextVar[User | None] = ContextVar("hylian_current_user", default=None)


def set_user(user: User | None) -> object:
    return _current_user.set(user)

def get_user() -> User | None:
    return _current_user.get()

def reset_user(token: object) -> None:
    _current_user.reset(token)  # type: ignore[arg-type]
