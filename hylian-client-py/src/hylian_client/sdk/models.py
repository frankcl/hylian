"""数据模型，字段名直接对齐 Java 端 JSON（均为 snake_case，无需 alias）。

未知字段一律忽略，保证服务端新增字段不破坏客户端。
"""

from __future__ import annotations

from pydantic import BaseModel, ConfigDict


class _Model(BaseModel):
    model_config = ConfigDict(extra="ignore", populate_by_name=True)


class Tenant(_Model):
    id: str | None = None
    name: str | None = None
    create_time: int | None = None
    update_time: int | None = None


class Role(_Model):
    id: str | None = None
    name: str | None = None
    app_id: str | None = None
    create_time: int | None = None
    update_time: int | None = None


class Permission(_Model):
    id: str | None = None
    name: str | None = None
    path: str | None = None
    app_id: str | None = None
    create_time: int | None = None
    update_time: int | None = None


class User(_Model):
    id: str | None = None
    username: str | None = None
    password: str | None = None
    name: str | None = None
    gender: bool | None = None
    email: str | None = None
    company: str | None = None
    province: str | None = None
    city: str | None = None
    district: str | None = None
    address: str | None = None
    industry: str | None = None
    position: str | None = None
    phone: str | None = None
    tenant_id: str | None = None
    avatar: str | None = None
    wx_openid: str | None = None
    disabled: bool | None = None
    register_mode: int | None = None
    super_admin: bool = False
    tenant: Tenant | None = None
    create_time: int | None = None
    update_time: int | None = None
