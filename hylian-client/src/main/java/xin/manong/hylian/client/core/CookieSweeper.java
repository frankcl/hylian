package xin.manong.hylian.client.core;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import xin.manong.hylian.client.common.Constants;
import xin.manong.hylian.client.util.CookieUtils;
import xin.manong.weapon.base.util.DomainUtil;

/**
 * cookie清理器
 * 认证异常发生时清理cookie登录信息
 *
 * @author frankcl
 * @date 2025-03-30 13:35:55
 */
public class CookieSweeper {

    /**
     * 清理cookie中登录信息
     *
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     */
    protected void sweepCookies(HttpServletRequest httpRequest,
                                HttpServletResponse httpResponse) {
        String domain = DomainUtil.getDomain(httpRequest.getServerName());
        if (!domain.startsWith(".")) domain = "." + domain;
        CookieUtils.removeCookie(Constants.COOKIE_TOKEN, "/", domain, httpResponse);
        CookieUtils.removeCookie(Constants.COOKIE_TICKET, "/", domain, httpResponse);
    }
}
