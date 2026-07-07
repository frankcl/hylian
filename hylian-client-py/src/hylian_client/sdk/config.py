"""客户端配置，对齐 Java ``hylian.client.*`` 与 ``hylian.filter.guard.*``。

环境变量前缀 ``HYLIAN_``，嵌套用 ``__``，例如::

    HYLIAN_APP_ID=...
    HYLIAN_APP_SECRET=...
    HYLIAN_SERVER_URL=https://sso.example.com:9001/
    HYLIAN_EXCLUDE_PATTERNS=["/health","/static/*"]
    HYLIAN_VERIFY_TLS=false
"""

from __future__ import annotations

from pydantic import Field, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class HylianClientConfig(BaseSettings):
    model_config = SettingsConfigDict(
        env_prefix="HYLIAN_",
        env_nested_delimiter="__",
        extra="ignore",
    )

    app_id: str = ""
    app_secret: str = ""
    server_url: str = ""

    # filter 行为：默认守卫全部路径，exclude 命中的路径放行（不鉴权）
    exclude_patterns: list[str] = Field(default_factory=list)
    # 客户端 logout 拦截路径，命中后 302 重定向到服务端 logout（对齐 Java /api/logout）
    logout_path: str = "/api/logout"
    # 服务端推送远程失效本地会话的拦截路径（对齐 Java /api/sweep）
    sweep_path: str = "/api/sweep"
    # 自签证书的 SSO（9001 端口）可关闭校验
    verify_tls: bool = True
    # 调服务端的超时（秒）
    timeout: float = 10.0

    # ---- cookie/session 模式配置 ----
    # 服务端会话 sid 的 cookie 名（对齐 Java 的 JSESSIONID 角色）
    session_cookie_name: str = "HYLIAN_SESSION"
    # 会话空闲超时（秒），超时回收并对 token 会话通知服务端 removeActivity（对齐 Java 1800s）
    session_idle_seconds: int = 1800
    # token 刷新最小间隔（秒），距上次刷新超过该值才向服务端换新 token（对齐 Java 60s）
    token_refresh_interval_seconds: int = 60
    # 会话 cookie 是否 Secure（HTTPS-only）；本地/自签开发可置 False
    session_cookie_secure: bool = True
    # 会话 cookie 的 SameSite 策略
    session_cookie_samesite: str = "lax"

    @field_validator("server_url")
    @classmethod
    def _ensure_trailing_slash(cls, v: str) -> str:
        if v and not v.endswith("/"):
            v += "/"
        return v

    def check(self) -> None:
        """校验必填项，缺失抛出 :class:`ValueError`。"""
        if not self.app_id:
            raise ValueError("应用ID(app_id)为空")
        if not self.app_secret:
            raise ValueError("应用秘钥(app_secret)为空")
        if not self.server_url:
            raise ValueError("安全检测服务URL(server_url)为空")
