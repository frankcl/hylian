# Hylian 单点登录(SSO)原理与流程

## 1. 系统概述

Hylian是一个基于Java的单点登录(SSO)系统，采用**中心化认证服务器 + 客户端SDK**的架构模式。系统支持两种认证方式：

- **基于Cookie的浏览器登录**：适用于Web应用，通过Cookie + Session维持登录状态
- **基于HTTP Header Token的API登录**：适用于API调用，通过`Authorization: Bearer <token>`传递令牌

### 1.1 模块结构

```
hylian/
├── hylian-model/     # 数据模型层：User、App、Role、Permission等实体定义
├── hylian-server/    # SSO认证服务器：负责用户认证、Ticket/Token签发与管理
└── hylian-client/    # 客户端SDK：集成到业务应用中，负责请求拦截与认证交互
```

### 1.2 核心概念

| 概念 | 说明 | 有效期 |
|------|------|--------|
| **Ticket** | 用户在SSO服务器的全局登录凭证(JWT格式)，存储在Cookie中 | 24小时(Cookie) / 12小时(缓存) |
| **Token** | 应用级别的访问令牌(JWT格式)，每个应用独立持有 | 10分钟(缓存) |
| **Code** | 一次性授权码，用于Ticket换取Token的中间凭证 | 60秒 |
| **App** | 接入SSO的业务应用，拥有唯一的`appId`和`appSecret` | - |

### 1.3 核心组件

**服务端(hylian-server)：**

| 组件 | 职责 |
|------|------|
| `SecurityController` | 安全控制器，提供登录、注销、Code申请、Token获取等API |
| `JWTService` | JWT服务，负责Ticket和Token的构建与解码 |
| `TicketService` | Ticket管理服务，负责Ticket的缓存、验证、关联Token管理 |
| `TokenService` | Token管理服务，负责Token的缓存、验证、与Ticket的映射 |
| `CodeService` | 授权码服务，负责Code的创建与Ticket映射 |
| `TicketTokenManagement` | Ticket/Token统一管理组件，协调Ticket和Token的生命周期 |
| `ActivityManagement` | 用户活动管理，记录用户在各应用的登录状态 |

**客户端(hylian-client)：**

| 组件 | 职责 |
|------|------|
| `HylianShield` | 安全检测核心，负责登录验证与认证流程调度 |
| `HylianGuard` | Servlet Filter实现，适用于非Spring MVC应用 |
| `HylianInterceptor` | Spring MVC拦截器实现，适用于Spring MVC应用 |
| `HylianClient` | HTTP客户端，负责与SSO服务器通信 |
| `SessionManager` | 会话管理器，维护本地Session映射 |
| `ContextManager` | 线程上下文管理器，存储当前请求的用户信息 |
| `ACLAspect` | 访问控制切面，基于权限的细粒度访问控制 |

---

## 2. 令牌体系

### 2.1 JWT结构

Hylian使用JWT(JSON Web Token)作为Ticket和Token的载体，采用**HMAC-SHA256**对称加密算法签名。

```
Header: { "alg": "HS256", "category": "ticket" | "token" }
Payload: { "profile": { "id": "<唯一ID>", "user_id": "<用户ID>" }, "exp": <过期时间> }
Signature: HMACSHA256(base64(header) + "." + base64(payload), secretKey)
```

`category`字段区分JWT的用途：`ticket`表示全局登录凭证，`token`表示应用级访问令牌。

### 2.2 Ticket与Token的关系

```
┌─────────────────────────────────────────────────────┐
│                    SSO Server                        │
│                                                      │
│  Ticket (全局凭证)                                    │
│    ├── Token-A (应用A的访问令牌)                       │
│    ├── Token-B (应用B的访问令牌)                       │
│    └── Token-C (应用C的访问令牌)                       │
│                                                      │
│  缓存结构:                                            │
│    TicketCache:  profileId → ticket                  │
│    TokenCache:   token → ticket (反向映射)            │
│    TicketToken:  profileId → Set<token> (关联token)   │
└─────────────────────────────────────────────────────┘
```

一个Ticket对应用户的一次全局登录，可关联多个Token(每个Token对应一个已登录的应用)。

---

## 3. 基于Cookie的浏览器登录流程

此模式适用于浏览器访问Web应用的场景，核心交互通过HTTP重定向和Cookie实现。

### 3.1 首次登录流程

```
用户浏览器          业务应用(Client)           SSO服务器(Server)
    │                    │                         │
    │ 1. 访问业务应用     │                         │
    │ ──────────────────>│                         │
    │                    │                         │
    │                    │ 2. HylianShield检测:     │
    │                    │    - Header无Token       │
    │                    │    - Session无Token      │
    │                    │    - URL无Code参数        │
    │                    │                         │
    │ 3. 303重定向到SSO  │                         │
    │ <──────────────────│                         │
    │   Location: /api/security/applyCode          │
    │   ?app_id=xxx&app_secret=xxx                 │
    │   &redirect_url=原始URL                       │
    │                    │                         │
    │ 4. 请求applyCode   │                         │
    │ ─────────────────────────────────────────────>│
    │                    │                         │ 5. 验证appId/appSecret
    │                    │                         │ 6. 检查Cookie中的Ticket
    │                    │                         │    → Ticket不存在
    │                    │                         │
    │ 7. SSO服务器自身拦截，                         │
    │    重定向到SSO登录页面                          │
    │ <─────────────────────────────────────────────│
    │                    │                         │
    │ 8. 用户输入用户名/密码                          │
    │ ─────────────────────────────────────────────>│
    │                    │                         │ 9. 验证用户名密码
    │                    │                         │ 10. 构建UserProfile
    │                    │                         │ 11. 签发Ticket(JWT)
    │                    │                         │ 12. 签发Token(JWT)
    │                    │                         │ 13. 设置Cookie:
    │                    │                         │     TICKET=<ticket> (httpOnly)
    │                    │                         │     TOKEN=<token>
    │                    │                         │ 14. 记录Activity
    │                    │                         │
    │ 15. 登录成功        │                         │
    │ <─────────────────────────────────────────────│
    │                    │                         │
    │ 16. 重新访问applyCode                          │
    │ ─────────────────────────────────────────────>│
    │                    │                         │ 17. 验证appId/appSecret
    │                    │                         │ 18. 从Cookie取Ticket
    │                    │                         │ 19. 验证Ticket有效性
    │                    │                         │ 20. 生成Code(绑定Ticket)
    │                    │                         │
    │ 21. 重定向回业务应用│                         │
    │ <─────────────────────────────────────────────│
    │   Location: 原始URL?code=xxx                  │
    │                    │                         │
    │ 22. 携带code访问    │                         │
    │ ──────────────────>│                         │
    │                    │ 23. HylianShield检测:     │
    │                    │     发现URL中有code参数    │
    │                    │                         │
    │                    │ 24. acquireToken         │
    │                    │ ────────────────────────>│
    │                    │                         │ 25. 验证appId/appSecret
    │                    │                         │ 26. 根据Code取Ticket
    │                    │                         │ 27. 验证Ticket
    │                    │                         │ 28. 签发新Token
    │                    │                         │ 29. 记录Activity
    │                    │ <────────────────────────│
    │                    │     返回token             │
    │                    │                         │
    │                    │ 30. 存Token到Session      │
    │                    │ 31. 记录为TokenSession    │
    │                    │                         │
    │ 32. 重定向(去除code)│                         │
    │ <──────────────────│                         │
    │                    │                         │
    │ 33. 正常访问        │                         │
    │ ──────────────────>│                         │
    │                    │ 34. Session中有Token      │
    │                    │ 35. 通过Token获取用户信息  │
    │                    │ 36. 设置到ContextManager  │
    │                    │                         │
    │ 37. 返回业务数据    │                         │
    │ <──────────────────│                         │
```

### 3.2 已登录SSO后访问新应用(SSO效果)

当用户已在SSO服务器登录(浏览器持有Ticket Cookie)，访问另一个业务应用时：

```
用户浏览器           新业务应用(Client)           SSO服务器(Server)
    │                    │                         │
    │ 1. 访问新应用       │                         │
    │ ──────────────────>│                         │
    │                    │ 2. Session无Token         │
    │                    │    URL无Code              │
    │                    │                         │
    │ 3. 303重定向到SSO   │                         │
    │ <──────────────────│                         │
    │                    │                         │
    │ 4. 请求applyCode（自动携带Ticket Cookie）       │
    │ ─────────────────────────────────────────────>│
    │                    │                         │ 5. 从Cookie取Ticket
    │                    │                         │ 6. 验证Ticket有效 ✓
    │                    │                         │ 7. 生成Code
    │                    │                         │
    │ 8. 重定向回新应用   │                         │
    │ <─────────────────────────────────────────────│
    │   Location: 新应用URL?code=xxx                │
    │                    │                         │
    │ 9. 携带code访问     │                         │
    │ ──────────────────>│                         │
    │                    │ 10. acquireToken          │
    │                    │ ────────────────────────>│
    │                    │ <────────────────────────│
    │                    │ 11. 存Token到Session      │
    │                    │                         │
    │ 12. 重定向(无感登录) │                         │
    │ <──────────────────│                         │
    │                    │                         │
    │ 13. 正常访问(已登录) │                         │
    │ ──────────────────>│                         │
```

**关键点：** 用户无需再次输入密码，浏览器自动携带SSO域下的Ticket Cookie，SSO服务器验证通过后直接签发Code，实现无感登录。

### 3.3 Token刷新机制

Token有效期为10分钟，客户端通过`refreshToken`机制自动续期：

```java
// HylianShield.refreshToken() - 每60秒检查一次是否需要刷新
if (refreshInterval > REFRESH_TIME_INTERVAL_MS) {  // 60秒
    String newToken = hylianClient.refreshToken(token);  // 向SSO服务器请求新Token
    SessionUtils.setToken(httpServletRequest, newToken);  // 更新Session中的Token
}
```

刷新流程：
1. 客户端移除旧的Token缓存映射
2. 基于原Ticket签发新Token
3. 建立新Token与Ticket的映射关系

### 3.4 Cookie说明

| Cookie名 | 域 | 作用 | httpOnly |
|-----------|---------|------|----------|
| `TICKET` | SSO服务器域 | 全局登录凭证，用于跨应用SSO | 是 |
| `TOKEN` | SSO服务器域 | SSO服务器自身的访问令牌 | 否 |

---

## 4. 基于HTTP Header Token的API登录流程

此模式适用于API调用场景(如微服务间调用、移动端、前后端分离架构)，不依赖Cookie和Session。

### 4.1 认证流程

```
API客户端                业务应用(Client)           SSO服务器(Server)
    │                        │                         │
    │ 1. 发送API请求           │                         │
    │   Header:               │                         │
    │   Authorization:        │                         │
    │     Bearer <token>      │                         │
    │ ──────────────────────>│                         │
    │                        │                         │
    │                        │ 2. HylianShield检测:     │
    │                        │    从Header提取Token     │
    │                        │    Authorization:        │
    │                        │      Bearer <token>      │
    │                        │                         │
    │                        │ 3. getUser(token)        │
    │                        │ ────────────────────────>│
    │                        │                         │ 4. 验证appId/appSecret
    │                        │                         │ 5. 验证Token(JWT签名+缓存)
    │                        │                         │ 6. 从Token解码UserProfile
    │                        │                         │ 7. 查询用户信息
    │                        │ <────────────────────────│
    │                        │     返回User对象          │
    │                        │                         │
    │                        │ 8. 设置User到Session     │
    │                        │ 9. 设置User到Context     │
    │                        │ 10. 执行业务逻辑          │
    │                        │                         │
    │ 11. 返回业务响应         │                         │
    │ <──────────────────────│                         │
```

### 4.2 Token提取逻辑

```java
// HTTPUtils.getTokenFromHeader()
public static String getTokenFromHeader(HttpServletRequest httpRequest) {
    String value = httpRequest.getHeader("Authorization");  // 获取Authorization头
    String prefix = "Bearer ";
    if (isEmpty(value) || !value.startsWith(prefix)) return null;
    return value.substring(prefix.length());  // 提取Bearer后的Token
}
```

### 4.3 与Cookie模式的区别

| 特性 | Cookie模式 | Header Token模式 |
|------|-----------|-----------------|
| Token来源 | Session中获取 | HTTP Header `Authorization: Bearer <token>` |
| 登录状态维持 | Cookie + Session自动管理 | 客户端需自行管理Token |
| 未认证处理 | 303重定向到SSO登录页 | 抛出`NotAuthorizedException`异常 |
| Token刷新 | 客户端自动刷新(60秒间隔) | 客户端需自行调用refreshToken |
| 适用场景 | 浏览器Web应用 | API调用、移动端、前后端分离 |
| Session依赖 | 是 | 否 |

### 4.4 HylianShield中的优先级

```java
// HylianShield.shelter() 中的认证优先级
public boolean shelter(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
    // 1. 优先检查HTTP Header中的Token (API模式)
    String token = HTTPUtils.getTokenFromHeader(httpRequest);
    if (isNotEmpty(token)) {
        User user = hylianClient.getUser(token);  // 直接向SSO验证
        if (user == null) throw new NotAuthorizedException("Token验证失败");
        SessionUtils.setUser(httpRequest, user);
        return true;
    }

    // 2. 其次检查Session中的Token (Cookie模式)
    token = SessionUtils.getToken(httpRequest);
    if (isNotEmpty(token)) {
        if (refreshUser(token, httpRequest) && refreshToken(token, httpRequest)) return true;
    }

    // 3. 最后检查URL中的Code (Cookie模式的回调阶段)
    String code = httpRequest.getParameter("code");
    if (isEmpty(code)) {
        // 无任何凭证，重定向到SSO申请Code
        httpResponse.setStatus(303);
        httpResponse.setHeader("Location", applyCodeURL);
        return false;
    }
    // 有Code，用Code换取Token...
}
```

---

## 5. 注销流程

### 5.1 单应用注销

```
用户浏览器           业务应用(Client)           SSO服务器(Server)
    │                    │                         │
    │ 1. 访问/api/logout  │                         │
    │ ──────────────────>│                         │
    │                    │ 2. HylianShield检测到     │
    │                    │    logout路径             │
    │                    │ 3. 重定向到SSO logout      │
    │                    │    /api/security/logout   │
    │                    │    ?app_id&app_secret     │
    │                    │ 4. 失效本地Session         │
    │                    │                         │
    │ 5. 请求SSO注销      │                         │
    │ ─────────────────────────────────────────────>│
    │                    │                         │ 6. 验证appId/appSecret
    │                    │                         │ 7. 从Cookie获取Ticket
    │                    │                         │ 8. 移除Ticket关联的所有Token
    │                    │                         │ 9. 移除Ticket缓存
    │                    │                         │ 10. 清除Cookie(TICKET+TOKEN)
    │                    │                         │ 11. 移除所有Activity记录
    │                    │                         │
    │ 12. 注销完成         │                         │
    │ <─────────────────────────────────────────────│
```

### 5.2 全局注销的关键

注销时SSO服务器执行`removeTicketTokensByTicket(ticket)`，会：
1. 解码Ticket中的`UserProfile`获取`profileId`
2. 通过`profileId`找到所有关联的Token并移除
3. 移除Ticket本身
4. 移除所有应用的Activity记录

这意味着该用户在**所有应用**中的Token都会失效，各应用在下次请求时Token验证失败，将触发重新登录。

---

## 6. Session清理机制

SSO服务器注销时，通知各应用清理本地Session：

```
SSO服务器                     业务应用(Client)
    │                              │
    │ 1. 调用/api/sweep             │
    │   ?session_id=<sessionId>    │
    │ ────────────────────────────>│
    │                              │ 2. HylianShield检测到sweep路径
    │                              │ 3. SessionManager.invalidate(sessionId)
    │                              │ 4. 触发SessionListener.sessionDestroyed
    │                              │ 5. 清除Session中的Token/User等资源
    │                              │ 6. 调用SSO removeActivity
    │                              │
```

### Session生命周期管理

```java
// SessionListener - 会话创建时
sessionCreated: {
    session.setAttribute("__LOCK__", new ReentrantLock());  // 设置会话锁
    SessionManager.put(session);                            // 注册到管理器
    session.setMaxInactiveInterval(1800);                   // 30分钟超时
}

// SessionListener - 会话销毁时
sessionDestroyed: {
    client.removeActivity(sessionId);  // 通知SSO移除Activity
    SessionManager.remove(session);    // 从管理器移除
}
```

---

## 7. 访问控制(ACL)

Hylian提供基于**角色-权限**模型的细粒度访问控制：

```
User ──N:M──> Role ──N:M──> Permission(URL Path)
                              │
                              └── 匹配请求路径，决定是否放行
```

通过`@EnableACLAspect`注解标注的Controller方法，会触发`ACLAspect`进行权限检查：

1. 从`ContextManager`获取当前用户
2. 查询用户的角色列表（通过SSO服务器API）
3. 查询角色对应的权限列表
4. 将请求路径与权限路径进行匹配
5. 匹配成功放行，否则抛出`ForbiddenException`

---

## 8. 客户端集成方式

Hylian客户端提供两种集成方式：

### 8.1 Servlet Filter模式 (`HylianGuard`)

通过`@EnableHylianGuard`注解启用，适用于所有Servlet容器：

```java
@EnableHylianGuard  // 启用Filter模式
@SpringBootApplication
public class MyApplication { }
```

支持URL排除模式配置，匹配排除路径的请求不经过SSO认证。

### 8.2 Spring MVC Interceptor模式 (`HylianInterceptor`)

通过`@EnableHylianInterceptor`注解启用，适用于Spring MVC应用：

```java
@EnableHylianInterceptor  // 启用Interceptor模式
@SpringBootApplication
public class MyApplication { }
```

### 8.3 客户端配置

```yaml
hylian:
  client:
    app-id: <应用ID>
    app-secret: <应用密钥>
    server-url: https://sso.example.com/
  filter:                                 # 针对 Servlet Filter 模式
    filterOrder: 1                        # 过滤器顺序，默认-1000
    includePatterns:                      # 拦截URL列表，默认拦截/*
      - /*
    excludePatterns:                      # 拦截排除URL列表
      - /xxx
      - /favicon.ico
```

---

## 9. 安全设计

| 安全措施 | 说明 |
|---------|------|
| JWT签名验证 | 所有Ticket和Token均使用HMAC-SHA256签名，防止篡改 |
| App身份验证 | 所有SSO API调用需携带`appId`和`appSecret`，防止未授权访问 |
| 一次性Code | Code仅使用一次且60秒过期，防止重放攻击 |
| Ticket httpOnly | Ticket Cookie设置httpOnly，防止XSS窃取 |
| HTTPS Secure | HTTPS环境下Cookie自动设置Secure标志 |
| Session锁 | Token获取使用`ReentrantLock`防止并发重复获取 |
| Token短有效期 | Token有效期10分钟，配合自动刷新机制，降低泄露风险 |
| Cookie清理 | 认证异常时自动清理Cookie中的登录信息 |
| 验证码 | 密码登录需要验证码校验，防止暴力破解 |