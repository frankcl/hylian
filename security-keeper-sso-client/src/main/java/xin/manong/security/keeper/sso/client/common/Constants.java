package xin.manong.security.keeper.sso.client.common;

/**
 * 常量定义
 *
 * @author frankcl
 * @date 2023-09-13 10:45:45
 */
public class Constants {

    public static final String CHARSET_UTF8 = "UTF-8";

    public static final String PARAM_CODE = "code";
    public static final String PARAM_TOKEN = "token";
    public static final String PARAM_APP_ID = "app_id";
    public static final String PARAM_APP_SECRET = "app_secret";
    public static final String PARAM_LOGOUT_URL = "logout_url";
    public static final String PARAM_SESSION_ID = "session_id";
    public static final String PARAM_REDIRECT_URL = "redirect_url";
    public static final String PARAM_SERVER_URL = "server_url";
    public static final String PARAM_EXCLUDE_PATTERNS = "exclude_patterns";

    public static final String CLIENT_PATH_LOGOUT = "/logout";
    public static final String CLIENT_PATH_LOGOUT_DESTROY = "/logout/destroy";

    public static final String SERVER_PATH_LOGIN = "home/login";
    public static final String SERVER_PATH_LOGOUT = "security/sso/logout";
    public static final String SERVER_PATH_APPLY_CODE = "security/auth/applyCode";
    public static final String SERVER_PATH_CHECK_TOKEN = "security/auth/checkToken";
    public static final String SERVER_PATH_ACQUIRE_TOKEN = "security/auth/acquireToken";
    public static final String SERVER_PATH_REFRESH_TOKEN = "security/auth/refreshToken";
    public static final String SERVER_PATH_REMOVE_APP_LOGIN = "security/auth/removeAppLogin";
    public static final String SERVER_PATH_GET_USER = "security/resource/getUser";
    public static final String SERVER_PATH_GET_TENANT = "security/resource/getTenant";
    public static final String SERVER_PATH_GET_VENDOR = "security/resource/getVendor";
}
