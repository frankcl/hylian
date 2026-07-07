# hylian-client (Python)

Python 版 Hylian 客户端 SDK。让 Python 应用（FastAPI）接入现有的 Java 版
`hylian-server`，完成单点登录鉴权与 ACL 访问控制。提供两种接入方式：

- **`enable_hylian_shield`（推荐，cookie/session 模式）** —— 对齐 Java 完整
  `HylianShield`：浏览器应用的完整 SSO 登录闭环，并把 **token 与用户信息缓存在服务端
  会话**中，避免每请求都往返 hylian-server。
- **`enable_hylian_guard`（纯 Bearer/API 模式）** —— 仅校验 `Authorization: Bearer`，
  无会话缓存，适合纯 API 服务；登录/登出由 hylian 产品自身负责。

## 能力

- **两种鉴权模式，统一入口按优先级分流**：`enable_hylian_shield` 每请求**先看
  `Authorization: Bearer`**——有就走 API 模式（逐请求 `getUser` 校验，失败 401）；
  没有才走 **session 模式**（会话缓存 token+user，`?code=` 换 token，未登录 303 重定向
  服务端 `applyCode`，token 距上次刷新超阈值才换新）。
- **会话缓存（session 管理）**：服务端会话保存 `token / user / roles / permissions`，
  浏览器仅持有 httpOnly 的 sid cookie（默认 `HYLIAN_SESSION`）。命中缓存即不再打服务端，
  显著减少交互。对齐 Java `HttpSession` + `SessionManager` + `SessionListener`。
- **全局登出 / 远程失效**：自动拦截 `/api/logout`（302 重定向服务端 logout）与
  `/api/sweep`（服务端推送按 `session_id` 远程失效本地会话）；token 会话销毁（登出/
  空闲超时）时通知服务端 `removeActivity` 注销应用登录活动。
- **ACL**：逐路由 opt-in 的 `acl()` 依赖，按用户权限路径匹配请求路径，无权返回 403；
  cookie 模式下权限亦缓存在会话中。
- **CORS**：复刻 Java `CORSFilter` 行为。

> 会话默认为**进程内内存存储**（`InMemorySessionStore`），仅单进程有效。多 worker /
> 多实例部署需传入共享的 `store` 实现（如 Redis）：`enable_hylian_shield(app, store=...)`。
> 存储接口 `SessionStore` 已预留。

## 安装

```bash
pip install "hylian-client[fastapi]"
```

从源码（本仓库 `hylian-client-py/` 目录）：

```bash
pip install -e ".[fastapi]"
```

## 配置（环境变量，前缀 `HYLIAN_`）

| 变量 | 说明 |
|---|---|
| `HYLIAN_APP_ID` | 应用 ID |
| `HYLIAN_APP_SECRET` | 应用秘钥 |
| `HYLIAN_SERVER_URL` | hylian-server 地址，如 `https://sso:9001/` |
| `HYLIAN_EXCLUDE_PATTERNS` | 放行（免鉴权）路径，如 `["/health","/static/*"]` |
| `HYLIAN_LOGOUT_PATH` | 登出拦截路径，默认 `/api/logout` |
| `HYLIAN_SWEEP_PATH` | 远程失效拦截路径，默认 `/api/sweep` |
| `HYLIAN_VERIFY_TLS` | 自签证书可设 `false` |
| `HYLIAN_SESSION_COOKIE_NAME` | 会话 sid cookie 名，默认 `HYLIAN_SESSION` |
| `HYLIAN_SESSION_IDLE_SECONDS` | 会话空闲超时（秒），默认 `1800` |
| `HYLIAN_TOKEN_REFRESH_INTERVAL_SECONDS` | token 刷新最小间隔（秒），默认 `60` |
| `HYLIAN_SESSION_COOKIE_SECURE` | 会话 cookie 是否 Secure，默认 `true`；本地/自签开发置 `false` |
| `HYLIAN_SESSION_COOKIE_SAMESITE` | 会话 cookie SameSite，默认 `lax` |

## 用法

### cookie/session 模式（推荐）

```python
from fastapi import FastAPI, Depends
from hylian_client import get_user
from hylian_client.sdk.fastapi import enable_hylian_shield, acl

app = FastAPI()
enable_hylian_shield(app)         # 统一 shield 中间件 + 会话缓存 + CORS

@app.get("/me")
def me():
    return {"user": get_user()}   # 未登录会被 303 重定向到服务端 applyCode

@app.get("/orders")
def orders(_: None = Depends(acl())):   # ACL：匹配请求路径
    return {"orders": []}
```

浏览器首次访问受守卫路径 → SDK 303 重定向到服务端 `applyCode` → 登录后带 `?code=`
回调 → SDK 换 token 并下发会话 cookie → 之后请求命中会话缓存，不再打服务端。
带 `Authorization: Bearer` 的请求则始终走 API 模式校验。

多实例共享会话（Redis 等）时自定义 `store`：

```python
enable_hylian_shield(app, store=MyRedisSessionStore())   # 实现 SessionStore 接口即可
```

### 纯 Bearer/API 模式

```python
from hylian_client.sdk.fastapi import enable_hylian_guard, acl

enable_hylian_guard(app)          # 仅校验 Bearer token，无会话缓存
```

## 登出

无需在应用里写任何路由。让浏览器访问客户端的 `/api/logout`（路径可经
`HYLIAN_LOGOUT_PATH` 配置），SDK 中间件会拦截并 302 重定向到 hylian 服务端
`api/security/logout`：

```
浏览器 GET /api/logout
  └─> SDK 302 → {server}/api/security/logout?app_id=...&app_secret=...
        └─> 浏览器带 SSO 域 TICKET cookie 命中服务端 logout
              └─> hylian 全局注销：删 ticket + 所有 token，清 TICKET/TOKEN cookie
```

注销由 hylian 产品完成，Python 侧只做拦截+重定向，不碰 cookie/session。注销后该用户
token 失效，后续请求 `getUser` 校验失败即返回 401。前端触发示例：

```js
window.location.href = "/api/logout";
```

## 测试

```bash
pip install -e ".[test]"
pytest
```

服务端调用全部用 `respx` mock，无需启动真实 hylian-server。
