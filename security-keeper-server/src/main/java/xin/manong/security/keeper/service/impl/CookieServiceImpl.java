package xin.manong.security.keeper.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.security.keeper.service.CookieService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * cookie服务实现
 *
 * @author frankcl
 * @date 2023-08-31 16:15:50
 */
@Service
public class CookieServiceImpl implements CookieService {

    private static final Logger logger = LoggerFactory.getLogger(CookieServiceImpl.class);

    @Override
    public String getCookie(HttpServletRequest httpRequest, String name) {
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

    @Override
    public void setCookie(String name, String value, String path,
                          HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        String schema = httpRequest.getScheme();
        if (!StringUtils.isEmpty(schema) && schema.equals("https")) cookie.setSecure(true);
        if (!StringUtils.isEmpty(path)) cookie.setPath(path);
        httpResponse.addCookie(cookie);
    }

    @Override
    public void removeCookie(String name, String path, HttpServletResponse httpResponse) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        if (!StringUtils.isEmpty(path)) cookie.setPath(path);
        httpResponse.addCookie(cookie);
    }
}
