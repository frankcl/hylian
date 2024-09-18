package xin.manong.security.keeper.sso.client.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
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

    private final AppClientConfig appClientConfig;
    private final SecurityChecker securityChecker;

    public SecurityInterceptor(AppClientConfig appClientConfig) {
        this.appClientConfig = appClientConfig;
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
     */
    @Override
    public boolean preHandle(@NotNull HttpServletRequest httpRequest,
                             @NotNull HttpServletResponse httpResponse,
                             @NotNull Object handler) throws Exception {
        addAllowOriginResponseHeaders(httpResponse);
        if (securityChecker.check(httpRequest, httpResponse)) {
            ContextManager.fillContext(httpRequest);
            return true;
        }
        return false;
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
        ContextManager.sweepContext();
    }

    /**
     * 设置跨域HTTP响应头
     *
     * @param httpResponse HTTP响应头
     */
    private void addAllowOriginResponseHeaders(HttpServletResponse httpResponse) {
        if (StringUtils.isEmpty(appClientConfig.allowOrigin)) return;
        httpResponse.addHeader("Access-Control-Allow-Origin", appClientConfig.allowOrigin);
        httpResponse.addHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
        httpResponse.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    }
}
