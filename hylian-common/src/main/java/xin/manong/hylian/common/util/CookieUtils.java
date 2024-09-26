package xin.manong.hylian.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * cookie工具
 *
 * @author frankcl
 * @date 2023-08-31 16:15:50
 */
public class CookieUtils {

    private static final Logger logger = LoggerFactory.getLogger(CookieUtils.class);

    /**
     * 获取cookie
     *
     * @param httpRequest HTTP请求
     * @param name cookie名称
     * @return 存在返回cookie值，否则返回null
     */
    public static String getCookie(HttpServletRequest httpRequest, String name) {
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies == null || cookies.length == 0) {
            logger.warn("cookie[{}] is not found", name);
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) return cookie.getValue();
        }
        logger.warn("cookie[{}] is not found", name);
        return null;
    }

    /**
     * 设置cookie
     *
     * @param name cookie名称
     * @param value cookie值
     * @param path cookie生效路径
     * @param httpOnly 只用于HTTP
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     */
    public static void setCookie(String name, String value, String path, boolean httpOnly,
                                 HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(httpOnly);
        String schema = httpRequest.getScheme();
        if (!StringUtils.isEmpty(schema) && schema.equals("https")) cookie.setSecure(true);
        if (!StringUtils.isEmpty(path)) cookie.setPath(path);
        httpResponse.addCookie(cookie);
    }

    /**
     * 移除cookie
     *
     * @param name cookie名称
     * @param path cookie生效路径
     * @param httpResponse HTTP响应
     */
    public static void removeCookie(String name, String path, HttpServletResponse httpResponse) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        if (!StringUtils.isEmpty(path)) cookie.setPath(path);
        httpResponse.addCookie(cookie);
    }
}
