package xin.manong.hylian.server.common;

/**
 * 常量定义
 *
 * @author frankcl
 * @date 2023-09-01 13:48:24
 */
public class Constants {

    public static final int DEFAULT_PAGE_NUM = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;

    public static final int LOCAL_CACHE_CAPACITY_CODE = 200;

    public static final Long CACHE_CODE_EXPIRED_TIME_MS = 60000L;
    public static final Long CACHE_TOKEN_EXPIRED_TIME_MS = 600000L;
    public static final Long CACHE_TICKET_EXPIRED_TIME_MS = 1800000L;
    public static final Long COOKIE_TICKET_EXPIRED_TIME_MS = 86400000L;

    public static final String CODE_CACHE_PREFIX = "__SK_CODE_";
    public static final String TOKEN_CACHE_PREFIX = "__SK_TOKEN_";
    public static final String TICKET_CACHE_PREFIX = "__SK_TICKET_";
    public static final String TICKET_TOKEN_PREFIX = "__SK_TICKET_TOKEN_";

    public static final String COOKIE_TICKET = "TICKET";
    public static final String COOKIE_TOKEN = "TOKEN";

    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static final String PREFIX_BEARER = "Bearer";
    public static final String PARAM_CODE = "code";

    public static final String JWT_HEADER_ALGORITHM = "alg";
    public static final String JWT_HEADER_CATEGORY = "category";
    public static final String JWT_CATEGORY_TICKET = "ticket";
    public static final String JWT_CATEGORY_TOKEN = "token";
    public static final String JWT_CLAIM_PROFILE = "profile";

    public static final String ALGORITHM_HS256 = "HS256";

    public static final String AVATAR_DIR = "avatar/";
    public static final String TEMP_AVATAR_DIR = "temp/avatar/";
}
