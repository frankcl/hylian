package xin.manong.security.keeper.sso.client.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import xin.manong.security.keeper.sso.client.config.AppClientConfig;
import xin.manong.security.keeper.sso.client.core.SecurityChecker;
import xin.manong.security.keeper.sso.client.core.ContextManager;

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

    public SecurityInterceptor(AppClientConfig appClientConfig) {
        securityChecker = new SecurityChecker(appClientConfig.appId,
                appClientConfig.appSecret, appClientConfig.serverURL);
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
    @Override
    public boolean preHandle(HttpServletRequest httpRequest,
                             HttpServletResponse httpResponse,
                             Object handler) throws Exception {
        boolean status = securityChecker.check(httpRequest, httpResponse);
        if (status) ContextManager.fillContext(httpRequest);
        return status;
    }

    /**
     * 完成拦截处理后清除线程上下文
     *
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @param handler 目标处理对象
     * @param e 异常对象
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest httpRequest,
                                HttpServletResponse httpResponse,
                                Object handler, Exception e) throws Exception {
        ContextManager.sweepContext();
    }
}
