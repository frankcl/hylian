"""CORS 支持：复刻 Java ``CORSFilter`` 行为。

Java 行为：反射回请求 ``Origin``、``Allow-Credentials: true``、预检 ``OPTIONS`` 放行
请求头与 ``GET/POST/PUT/DELETE/OPTIONS``。用 Starlette ``CORSMiddleware`` 等价实现。
"""

from __future__ import annotations

from fastapi import FastAPI
from starlette.middleware.cors import CORSMiddleware

_METHODS = ["GET", "POST", "PUT", "DELETE", "OPTIONS"]


def add_cors(app: FastAPI, origins: list[str] | None = None) -> None:
    """给 app 装上 CORS。

    ``origins`` 为空时反射任意 Origin（等价 Java 的反射行为），并允许携带凭据。
    指定 ``origins`` 则只放行白名单。
    """
    if origins:
        app.add_middleware(
            CORSMiddleware,
            allow_origins=origins,
            allow_credentials=True,
            allow_methods=_METHODS,
            allow_headers=["*"],
        )
    else:
        app.add_middleware(
            CORSMiddleware,
            allow_origin_regex=".*",
            allow_credentials=True,
            allow_methods=_METHODS,
            allow_headers=["*"],
        )
