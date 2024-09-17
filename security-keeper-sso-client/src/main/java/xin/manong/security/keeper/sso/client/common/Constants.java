package xin.manong.security.keeper.sso.client.common;

/**
 * 常量定义
 *
 * @author frankcl
 * @date 2023-09-13 10:45:45
 */
public class Constants {

    public static final String PARAM_CODE = "code";
    public static final String PARAM_TOKEN = "token";
    public static final String PARAM_APP_ID = "app_id";
    public static final String PARAM_APP_SECRET = "app_secret";
    public static final String PARAM_REDIRECT_URL = "redirect_url";
    public static final String PARAM_SESSION_ID = "session_id";
    public static final String PARAM_SERVER_URL = "server_url";
    public static final String PARAM_EXCLUDE_PATTERNS = "exclude_patterns";
    public static final String PARAM_USER_ID = "user_id";
    public static final String PARAM_ROLE_IDS = "role_ids";

    public static final String CLIENT_PATH_LOGOUT = "/api/logout";
    public static final String CLIENT_PATH_SWEEP = "/api/sweep";

    public static final String SERVER_PATH_LOGOUT = "api/security/logout";
    public static final String SERVER_PATH_APPLY_CODE = "api/security/applyCode";
    public static final String SERVER_PATH_CHECK_TOKEN = "api/security/checkToken";
    public static final String SERVER_PATH_ACQUIRE_TOKEN = "api/security/acquireToken";
    public static final String SERVER_PATH_REFRESH_TOKEN = "api/security/refreshToken";
    public static final String SERVER_PATH_REMOVE_APP_LOGIN = "api/security/removeAppLogin";
    public static final String SERVER_PATH_GET_USER = "api/security/getUser";
    public static final String SERVER_PATH_GET_TENANT = "api/security/getTenant";
    public static final String SERVER_PATH_GET_VENDOR = "api/security/getVendor";
    public static final String SERVER_PATH_GET_APP_USER_ROLES = "api/security/getAppUserRoles";
    public static final String SERVER_PATH_GET_APP_ROLE_PERMISSIONS = "api/security/getAppRolePermissions";

    public static final String CURRENT_USER = "__user__";
    public static final String CURRENT_TENANT = "__tenant__";
    public static final String CURRENT_VENDOR = "__vendor__";

    public static final String CHARSET_UTF8 = "UTF-8";
}
