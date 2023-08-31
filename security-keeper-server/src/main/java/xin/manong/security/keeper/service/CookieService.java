package xin.manong.security.keeper.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * cookie服务接口定义
 *
 * @author frankcl
 * @date 2023-08-31 16:14:00
 */
public interface CookieService {

    /**
     * 获取cookie
     *
     * @param httpRequest HTTP请求
     * @param name cookie名称
     * @return 存在返回cookie值，否则返回null
     */
    String getCookie(HttpServletRequest httpRequest, String name);

    /**
     * 设置cookie
     *
     * @param name cookie名称
     * @param value cookie值
     * @param path cookie生效路径
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     */
    void setCookie(String name, String value, String path,
                   HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    /**
     * 移除cookie
     *
     * @param name cookie名称
     * @param path cookie生效路径
     * @param httpResponse HTTP响应
     */
    void removeCookie(String name, String path, HttpServletResponse httpResponse);
}
