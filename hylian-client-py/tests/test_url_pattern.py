"""排除模式编译与匹配，对齐 Java HylianGuardConfig / HylianGuard。"""

import pytest

from hylian_client.sdk.url_pattern import build_exclude_patterns, match_exclude, process_exclude_pattern


def test_normal_exact():
    p = process_exclude_pattern("/health")
    assert not p.regex
    assert p.matches("/health")
    assert not p.matches("/health/x")


def test_normal_adds_leading_slash():
    p = process_exclude_pattern("health")
    assert p.pattern == "/health"


def test_trailing_star_prefix():
    p = process_exclude_pattern("/static/*")
    assert p.regex
    assert p.matches("/static/a.js")
    assert p.matches("/static/a/b.js")  # 正则 /static/.* 整串匹配，多级也命中
    assert not p.matches("/other/a.js")


def test_leading_star_suffix():
    p = process_exclude_pattern("*.js")
    assert p.regex
    assert p.matches("/static/a.js")
    assert p.matches("xajs")  # 正则 .*.js => '.' 通配 + 'js'
    assert not p.matches("/a.css")


@pytest.mark.parametrize("bad", ["/a/*/b", "/a*b", "**"])
def test_illegal(bad):
    with pytest.raises(ValueError):
        process_exclude_pattern(bad)


def test_match_exclude_list():
    patterns = build_exclude_patterns(["/health", "/static/*"])
    assert match_exclude("/health", patterns)
    assert match_exclude("/static/x.css", patterns)
    assert not match_exclude("/api/orders", patterns)
    assert not match_exclude("/api/orders", [])
