package xin.manong.security.keeper.sso.client.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import xin.manong.security.keeper.sso.client.core.SecurityChecker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 安全拦截器
 *
 * @author frankcl
 * @date 2023-09-05 19:34:12
 */
public class SecurityInterceptor implements HandlerInterceptor {

    private SecurityChecker securityChecker;

    public SecurityInterceptor(String appId,
                               String appSecret,
                               String serverURL) {
        securityChecker = new SecurityChecker(appId, appSecret, serverURL);
    }

    /**
     * 登录预处理方法
     *
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @param handler 目标处理对象
     * @return 检测成功返回true，否则返回false
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                             Object handler) throws Exception {
        return securityChecker.check(httpRequest, httpResponse);
    }
}
