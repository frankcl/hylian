"""移植自 Java ``PermissionUtilsTest``，作为协议回归基准。"""

import pytest

from hylian_client.sdk import permission
from hylian_client.sdk.permission import PermissionError


def test_match_exact():
    assert permission.match("/a/b", "/a/b")
    assert not permission.match("/a/b", "/a/c")


def test_match_single_star():
    assert permission.match("/a/*", "/a/b")
    assert not permission.match("/a/*", "/a/b/c")  # 仅单级
    assert not permission.match("/a/*", "/a")


def test_match_double_star():
    assert permission.match("/a/**", "/a/b")
    assert permission.match("/a/**", "/a/b/c")
    assert permission.match("/a/**", "/a/")


def test_match_normalizes_leading_slash():
    assert permission.match("/a/b", "a/b")


def test_match_empty():
    assert not permission.match("", "/a")
    assert not permission.match("/a", "")
    assert not permission.match(None, "/a")


def test_validate_ok():
    permission.validate("/a/b")
    permission.validate("/a/*")
    permission.validate("/a/**")


@pytest.mark.parametrize("bad", ["", "a/b", "/a/*/b", "/a*"])
def test_validate_bad(bad):
    with pytest.raises(PermissionError):
        permission.validate(bad)
