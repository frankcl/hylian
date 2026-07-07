"""hylian-client cookie/session 模式示例（FastAPI）。

对齐 Java 完整 ``HylianShield``：浏览器应用的 SSO 登录闭环 + token/用户会话缓存。

运行前配置环境变量指向你的 hylian-server：

    export HYLIAN_APP_ID=your-app-id
    export HYLIAN_APP_SECRET=your-app-secret
    export HYLIAN_SERVER_URL=https://your-sso-host:9001/
    export HYLIAN_EXCLUDE_PATTERNS='["/health"]'
    export HYLIAN_VERIFY_TLS=false                  # 自签证书时
    export HYLIAN_SESSION_COOKIE_SECURE=false       # 本地 http 调试时（否则 cookie 不回传）

启动：

    uvicorn examples.shield_app:app --reload --port 8080

浏览器访问 http://localhost:8080/me ：
1. 未登录 → SDK 303 重定向到服务端 applyCode；
2. 登录后服务端带 ?code= 回调本地 → SDK 换 token、下发会话 cookie、303 回干净 URL；
3. 之后请求命中会话缓存的 token+user，不再往返 hylian-server；
4. 访问 /api/logout → 302 重定向服务端 logout，完成全局注销并失效本地会话。

单进程内存会话仅演示用；多 worker/多实例部署需传入共享的 store（如 Redis）。
"""

from __future__ import annotations

from fastapi import Depends, FastAPI

from hylian_client.sdk import User, get_user
from hylian_client.sdk.fastapi import acl, enable_hylian_shield

app = FastAPI(title="hylian-client shield demo")

# 一行启用：统一 shield 中间件（先 Bearer 后 session）+ 会话缓存 + CORS。
client = enable_hylian_shield(app)


@app.get("/health")
def health():
    """exclude 命中的放行路径：无需登录。"""
    return {"status": "ok"}


@app.get("/me")
def me() -> dict:
    """受守卫路径：未登录会被 303 重定向到服务端 applyCode。

    登录后当前用户已从会话缓存注入上下文，用 ``get_user()`` 取出。
    """
    user: User | None = get_user()
    return {
        "id": user.id if user else None,
        "username": user.username if user else None,
        "name": user.name if user else None,
        "tenant": user.tenant.name if user and user.tenant else None,
    }


@app.get("/orders")
def list_orders(_: None = Depends(acl())):
    """ACL 保护路径：还要求当前用户拥有覆盖本路径(/orders)的权限，否则 403。

    cookie 模式下权限也缓存在会话中，重复访问不再拉取。
    """
    user = get_user()
    return {"owner": user.username, "orders": []}
