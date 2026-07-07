"""URL 排除模式，移植 Java ``URLPattern`` + ``HylianGuardConfig.processExcludePattern``
与 ``HylianGuard.matchExcludePath``。

支持三类模式：
- 无 ``*``：精确匹配（自动补前导 ``/``）
- 前导 ``*``：如 ``*.js`` → 正则 ``.*.js``（整串匹配）
- 末尾 ``/*``：如 ``/public/*`` → 正则 ``/public/.*``（整串匹配）
"""

from __future__ import annotations

import re
from dataclasses import dataclass


@dataclass
class URLPattern:
    regex: bool
    pattern: str
    _matcher: re.Pattern[str] | None = None

    @classmethod
    def build_normal(cls, pattern: str) -> "URLPattern":
        return cls(regex=False, pattern=pattern)

    @classmethod
    def build_regex(cls, pattern: str) -> "URLPattern":
        return cls(regex=True, pattern=pattern, _matcher=re.compile(pattern))

    def matches(self, path: str) -> bool:
        if self.regex:
            assert self._matcher is not None
            return self._matcher.fullmatch(path) is not None
        return path == self.pattern


def process_exclude_pattern(pattern: str) -> URLPattern:
    """把配置里的排除模式字符串编译成 :class:`URLPattern`。

    非法模式抛出 :class:`ValueError`，对齐 Java 的 ``IllegalStateException``。
    """
    p = pattern.find("*")
    if p == -1:
        normal = pattern if pattern.startswith("/") else f"/{pattern}"
        return URLPattern.build_normal(normal)
    n = pattern.count("*")
    length = len(pattern)
    if n == 1 and length > 1 and (p == 0 or (p == length - 1 and pattern[length - 2] == "/")):
        temp = f"{pattern[:p]}.{pattern[p:]}"
        if p == 0:
            return URLPattern.build_regex(temp)
        return URLPattern.build_regex(temp if temp.startswith("/") else f"/{temp}")
    raise ValueError(f"非法URL排除模式[{pattern}]")


def build_exclude_patterns(patterns: list[str] | None) -> list[URLPattern]:
    if not patterns:
        return []
    return [process_exclude_pattern(p) for p in patterns]


def match_exclude(path: str, patterns: list[URLPattern]) -> bool:
    return any(p.matches(path) for p in patterns)
