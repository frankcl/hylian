"""WebResponse 信封，对齐 weapon-jersey 的 ``WebResponse``。

服务端所有响应被包成 ``{status, code, message, request_id, data}``。
"""

from __future__ import annotations

from typing import Any, Generic, TypeVar

from pydantic import BaseModel, ConfigDict

T = TypeVar("T")


class WebResponse(BaseModel, Generic[T]):
    model_config = ConfigDict(extra="ignore")

    status: bool = False
    code: int | None = None
    message: str | None = None
    request_id: str | None = None
    data: T | None = None

    @classmethod
    def parse(cls, payload: dict[str, Any]) -> "WebResponse[Any]":
        return cls.model_validate(payload)
