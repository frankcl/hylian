package xin.manong.hylian.client.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.NotAuthorizedException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.HandlerInterceptor;
import xin.manong.hylian.client.core.CookieSweeper;
import xin.manong.hylian.client.core.HylianClient;
import xin.manong.hylian.client.core.HylianShield;
import xin.manong.hylian.client.core.ContextManager;
import xin.manong.hylian.client.util.HTTPUtils;
import xin.manong.hylian.client.util.SessionUtils;
import xin.manong.hylian.model.User;

/**
 * Hylian拦截器：负责安全拦截
 *
 * @author frankcl
 * @date 2023-09-05 19:34:12
 */
public class HylianInterceptor extends CookieSweeper implements HandlerInterceptor {

    private final HylianClient client;
    private final HylianShield shield;

    public HylianInterceptor(HylianClient client) {
        this.client = client;
        shield = new HylianShield(client);
    }

    /**
     * 登录预处理方法
     *
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @param handler 目标处理对象
     * @return 检测成功返回true，否则返回false
     */
    @Override
    public boolean preHandle(@NotNull HttpServletRequest httpRequest,
                             @NotNull HttpServletResponse httpResponse,
                             @NotNull Object handler) throws Exception {
        try {
            if (shield.shelter(httpRequest, httpResponse)) {
                ContextManager.setUser(getUser(httpRequest));
                return true;
            }
            return false;
        } catch (NotAuthorizedException e) {
            sweepCookies(httpRequest, httpResponse);
            throw e;
        }
    }

    /**
     * 完成拦截处理后清除线程上下文
     *
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @param handler 目标处理对象
     * @param e 异常对象
     */
    @Override
    public void afterCompletion(@NotNull HttpServletRequest httpRequest,
                                @NotNull HttpServletResponse httpResponse,
                                @NotNull Object handler, Exception e) throws Exception {
        ContextManager.removeUser();
        ContextManager.sweepContext();
    }

    /**
     * 获取用户信息
     * 1. 从session中获取用户信息
     * 2. 从header中获取token，通过token获取用户信息
     *
     * @param httpRequest HTTP请求
     * @return 用户信息
     */
    private User getUser(HttpServletRequest httpRequest) {
        User user = SessionUtils.getUser(httpRequest);
        if (user != null) return user;
        String token = HTTPUtils.getTokenFromHeader(httpRequest);
        if (StringUtils.isEmpty(token)) return null;
        return client.getUser(token);
    }
}
