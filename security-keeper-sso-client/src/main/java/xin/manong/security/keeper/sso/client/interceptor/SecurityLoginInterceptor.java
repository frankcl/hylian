package xin.manong.security.keeper.sso.client.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import xin.manong.security.keeper.sso.client.core.SecurityChecker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author frankcl
 * @date 2023-09-05 19:34:12
 */
public class SecurityLoginInterceptor implements HandlerInterceptor {

    private SecurityChecker securityChecker;

    public SecurityLoginInterceptor(String appId,
                                    String appSecret,
                                    String serverURL) {
        securityChecker = new SecurityChecker(appId, appSecret, serverURL);
    }

    /**
     * 登录预处理方法
     *
     * @param httpRequest
     * @param httpResponse
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                             Object handler) throws Exception {
        return securityChecker.check(httpRequest, httpResponse);
    }
}
