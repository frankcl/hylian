package xin.manong.security.keeper.common.util;

import org.apache.commons.lang3.StringUtils;
import xin.manong.security.keeper.common.SessionConstants;
import xin.manong.security.keeper.model.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * session工具
 *
 * @author frankcl
 * @date 2023-09-04 14:53:57
 */
public class SessionUtils {

    /**
     * 获取session ID
     *
     * @param httpRequest HTTP请求
     * @return 成功返回sessionID，否则返回null
     */
    public static String getSessionID(HttpServletRequest httpRequest) {
        HttpSession httpSession = httpRequest.getSession();
        return httpSession == null ? null : httpSession.getId();
    }

    /**
     * 从session中获取token
     *
     * @param httpRequest HTTP请求
     * @return 成功返回token，否则返回null
     */
    public static String getToken(HttpServletRequest httpRequest) {
        if (httpRequest == null) return null;
        return (String) httpRequest.getSession().getAttribute(SessionConstants.TOKEN);
    }

    /**
     * 从session中获取用户信息
     *
     * @param httpRequest HTTP请求
     * @return 成功返回用户信息，否则返回null
     */
    public static User getUser(HttpServletRequest httpRequest) {
        if (httpRequest == null) return null;
        return (User) httpRequest.getSession().getAttribute(SessionConstants.USER);
    }

    /**
     * 从session中获取租户信息
     *
     * @param httpRequest HTTP请求
     * @return 成功返回租户信息，否则返回null
     */
    public static Tenant getTenant(HttpServletRequest httpRequest) {
        if (httpRequest == null) return null;
        return (Tenant) httpRequest.getSession().getAttribute(SessionConstants.TENANT);
    }

    /**
     * 从session中获取供应商信息
     *
     * @param httpRequest HTTP请求
     * @return 成功返回供应商信息，否则返回null
     */
    public static Vendor getVendor(HttpServletRequest httpRequest) {
        if (httpRequest == null) return null;
        return (Vendor) httpRequest.getSession().getAttribute(SessionConstants.VENDOR);
    }

    /**
     * 从session中获取角色列表
     *
     * @param httpRequest HTTP请求
     * @return 成功返回角色列表，否则返回null
     */
    public static List<Role> getRoles(HttpServletRequest httpRequest) {
        if (httpRequest == null) return null;
        return (List<Role>) httpRequest.getSession().getAttribute(SessionConstants.ROLES);
    }

    /**
     * 从session中获取权限列表
     *
     * @param httpRequest HTTP请求
     * @return 成功返回权限列表，否则返回null
     */
    public static List<Permission> getPermissions(HttpServletRequest httpRequest) {
        if (httpRequest == null) return null;
        return (List<Permission>) httpRequest.getSession().getAttribute(SessionConstants.PERMISSIONS);
    }

    /**
     * 将token设置到session
     *
     * @param httpRequest HTTP请求
     * @param token
     */
    public static void setToken(HttpServletRequest httpRequest, String token) {
        if (httpRequest == null || StringUtils.isEmpty(token)) return;
        httpRequest.getSession().setAttribute(SessionConstants.TOKEN, token);
    }

    /**
     * 将用户信息设置到session
     *
     * @param httpRequest HTTP请求
     * @param user 用户信息
     */
    public static void setUser(HttpServletRequest httpRequest, User user) {
        if (httpRequest == null || user == null) return;
        httpRequest.getSession().setAttribute(SessionConstants.USER, user);
    }

    /**
     * 将租户信息设置到session
     *
     * @param httpRequest HTTP请求
     * @param tenant 租户信息
     */
    public static void setTenant(HttpServletRequest httpRequest, Tenant tenant) {
        if (httpRequest == null || tenant == null) return;
        httpRequest.getSession().setAttribute(SessionConstants.TENANT, tenant);
    }

    /**
     * 将供应商信息设置到session
     *
     * @param httpRequest HTTP请求
     * @param vendor 供应商信息
     */
    public static void setVendor(HttpServletRequest httpRequest, Vendor vendor) {
        if (httpRequest == null || vendor == null) return;
        httpRequest.getSession().setAttribute(SessionConstants.VENDOR, vendor);
    }

    /**
     * 将角色列表设置到session
     *
     * @param httpRequest HTTP请求
     * @param roles 角色列表
     */
    public static void setRoles(HttpServletRequest httpRequest, List<Role> roles) {
        if (httpRequest == null || roles == null || roles.isEmpty()) return;
        httpRequest.getSession().setAttribute(SessionConstants.ROLES, roles);
    }

    /**
     * 将权限列表设置到session
     *
     * @param httpRequest HTTP请求
     * @param permissions 权限列表
     */
    public static void setPermissions(HttpServletRequest httpRequest, List<Permission> permissions) {
        if (httpRequest == null || permissions == null || permissions.isEmpty()) return;
        httpRequest.getSession().setAttribute(SessionConstants.PERMISSIONS, permissions);
    }

    /**
     * 移除session资源
     *
     * @param httpRequest HTTP请求
     */
    public static void removeResources(HttpServletRequest httpRequest) {
        if (httpRequest == null) return;
        HttpSession httpSession = httpRequest.getSession();
        httpSession.removeAttribute(SessionConstants.TOKEN);
        httpSession.removeAttribute(SessionConstants.USER);
        httpSession.removeAttribute(SessionConstants.TENANT);
        httpSession.removeAttribute(SessionConstants.VENDOR);
        httpSession.removeAttribute(SessionConstants.PERMISSIONS);
    }
}
