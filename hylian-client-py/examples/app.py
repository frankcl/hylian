"""hylian-client 使用示例（FastAPI）。

运行前先配置环境变量指向你的 hylian-server：

    export HYLIAN_APP_ID=your-app-id
    export HYLIAN_APP_SECRET=your-app-secret
    export HYLIAN_SERVER_URL=https://your-sso-host:9001/
    export HYLIAN_EXCLUDE_PATTERNS='["/health","/public/*"]'
    export HYLIAN_VERIFY_TLS=false        # 自签证书时

启动：

    uvicorn examples.app:app --reload --port 8080

调用（token 由 hylian 产品登录后下发）：

    curl http://localhost:8080/health                              # 放行，无需 token
    curl http://localhost:8080/me     -H "Authorization: Bearer <token>"
    curl http://localhost:8080/orders -H "Authorization: Bearer <token>"

登出：直接让浏览器访问 /api/logout，SDK 中间件会自动拦截并 302 重定向到 hylian
服务端 logout（无需在应用里写任何路由）。由 hylian 完成全局注销。

    # 浏览器里跳转或前端 location.href 即可；带 -L 跟随重定向观察：
    curl -iL http://localhost:8080/api/logout

刷新 token：token 临近过期时，API 调用方可用旧 token 换取新 token（对齐 Java
``HylianClient.refreshToken``），避免重新走登录流程。中间件不做自动刷新，需调用方
主动调用。下面 /refresh-token 路由演示了这一用法：

    curl -X POST http://localhost:8080/refresh-token \\
         -H "Authorization: Bearer <token>"
    # 返回 {"token": "<新token>"}；旧 token 已失效时返回 401。
"""

from __future__ import annotations

from fastapi import Depends, FastAPI, HTTPException, Request

from hylian_client.sdk import User, get_user
from hylian_client.sdk.fastapi import acl, enable_hylian_guard

app = FastAPI(title="hylian-client demo")

# 一行启用：自动挂上 token 鉴权中间件 + CORS，配置从 HYLIAN_* 环境变量读取。
# 等价于 Java 端的 @EnableHylianGuard。返回构建好的 HylianClient，下面刷新 token 时复用。
client = enable_hylian_guard(app)


@app.get("/health")
def health():
    """exclude 命中的放行路径：无需 token 即可访问。"""
    return {"status": "ok"}


@app.get("/me")
def me() -> dict:
    """守卫路径：必须带合法 Bearer token，否则中间件直接返回 401。

    校验通过后，当前用户已注入上下文，用 ``get_user()`` 取出。
    """
    user: User | None = get_user()
    return {
        "id": user.id,
        "username": user.username,
        "name": user.name,
        "super_admin": user.super_admin,
        "tenant": user.tenant.name if user and user.tenant else None,
    }


@app.post("/refresh-token")
def refresh_token(request: Request) -> dict:
    """守卫路径：用当前请求里的旧 token 换取新 token。

    中间件已校验过 Authorization 头，这里直接取出旧 token 调用 ``client.refresh_token``；
    旧 token 已失效（服务端返回 null）时回 401，提示前端重新登录。
    """
    old_token = request.headers.get("Authorization", "")[len("Bearer "):]
    new_token = client.refresh_token(old_token)
    if new_token is None:
        raise HTTPException(status_code=401, detail="Token 刷新失败，请重新登录")
    return {"token": new_token}


@app.get("/orders")
def list_orders(_: None = Depends(acl())):
    """ACL 保护路径：除了登录，还要求当前用户拥有覆盖本路径(/orders)的权限，

    否则返回 403。``acl()`` 默认用请求实际路径匹配；也可 ``acl("/orders/**")`` 显式指定。
    """
    user = get_user()
    return {"owner": user.username, "orders": []}


@app.get("/admin")
def admin_only(_: None = Depends(acl("/admin/**"))):
    """显式指定受控资源路径的写法。"""
    return {"area": "admin"}
