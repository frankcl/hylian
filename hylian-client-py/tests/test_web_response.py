from hylian_client.sdk.web_response import WebResponse


def test_parse_ok():
    r = WebResponse.parse(
        {"status": True, "code": 200, "request_id": "x", "data": {"id": "u1"}}
    )
    assert r.status
    assert r.data == {"id": "u1"}


def test_parse_error():
    r = WebResponse.parse({"status": False, "code": 401, "message": "fail"})
    assert not r.status
    assert r.data is None
    assert r.message == "fail"


def test_ignores_unknown_fields():
    r = WebResponse.parse({"status": True, "data": 1, "extra": "ignored"})
    assert r.status and r.data == 1
