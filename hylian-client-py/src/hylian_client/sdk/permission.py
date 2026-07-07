"""资源路径权限工具，1:1 移植 Java ``PermissionUtils``。"""

from __future__ import annotations


class PermissionError(ValueError):
    """非法资源路径。"""


def validate(pattern: str | None) -> None:
    """校验资源路径合法性，非法抛出 :class:`PermissionError`。

    只允许以 ``/`` 开头，且通配符只能是结尾的 ``/*`` 或 ``/**``。
    """
    if not pattern:
        raise PermissionError("资源路径为空")
    if not pattern.startswith("/"):
        raise PermissionError("资源路径必须以 / 开始")
    stripped = pattern
    if stripped.endswith("/*"):
        stripped = stripped[:-2]
    elif stripped.endswith("/**"):
        stripped = stripped[:-3]
    if "*" in stripped:
        raise PermissionError("非法资源路径，只能以 /* 或 /** 结尾")


def match(pattern: str | None, path: str | None) -> bool:
    """判断访问路径 ``path`` 是否符合权限模式 ``pattern``。

    - ``/a/**`` 前缀匹配（含多级）
    - ``/a/*``  仅匹配单级
    - 其它      全等匹配
    """
    if not pattern or not path:
        return False
    if not path.startswith("/"):
        path = f"/{path}"
    if pattern.endswith("/**"):
        prefix = pattern[:-2]  # 保留末尾的 '/'
        return path.startswith(prefix)
    if pattern.endswith("/*"):
        prefix = pattern[:-1]  # 保留末尾的 '/'
        if not path.startswith(prefix):
            return False
        rest = path[len(prefix):]
        return "/" not in rest
    return pattern == path
