"""常量定义，对齐 Java 端 ``xin.manong.hylian.client.common.Constants``。

同时支持 Bearer/API 模式与 cookie/session 模式两套常量。
"""

PREFIX_BEARER = "Bearer"

HEADER_AUTHORIZATION = "Authorization"
HEADER_LOCATION = "Location"

PARAM_CODE = "code"
PARAM_TOKEN = "token"
PARAM_APP_ID = "app_id"
PARAM_APP_SECRET = "app_secret"
PARAM_REDIRECT_URL = "redirect_url"
PARAM_SESSION_ID = "session_id"
PARAM_USER_ID = "user_id"
PARAM_USER_IDS = "user_ids"
PARAM_ROLE_IDS = "role_ids"

# 客户端拦截路径：命中后重定向到服务端 logout（对齐 Java CLIENT_PATH_LOGOUT）
CLIENT_PATH_LOGOUT = "/api/logout"
# 客户端拦截路径：服务端推送远程失效本地会话（对齐 Java CLIENT_PATH_SWEEP）
CLIENT_PATH_SWEEP = "/api/sweep"

# 服务端接口路径（serverURL 以 / 结尾，故此处不带前导 /）
SERVER_PATH_LOGOUT = "api/security/logout"
SERVER_PATH_APPLY_CODE = "api/security/applyCode"
SERVER_PATH_ACQUIRE_TOKEN = "api/security/acquireToken"
SERVER_PATH_REFRESH_TOKEN = "api/security/refreshToken"
SERVER_PATH_REMOVE_ACTIVITY = "api/security/removeActivity"
SERVER_PATH_GET_USER = "api/security/getUser"
SERVER_PATH_GET_TENANT = "api/security/getTenant"
SERVER_PATH_GET_USERS = "api/security/getUsers"
SERVER_PATH_BATCH_GET_USERS = "api/security/batchGetUsers"
SERVER_PATH_GET_USER_BY_ID = "api/security/getUserById"
SERVER_PATH_GET_APP_USER_ROLES = "api/security/getAppUserRoles"
SERVER_PATH_GET_APP_ROLE_PERMISSIONS = "api/security/getAppRolePermissions"
SERVER_PATH_IS_APP_ADMIN = "api/security/isAppAdmin"
