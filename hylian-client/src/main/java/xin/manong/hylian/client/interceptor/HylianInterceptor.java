package xin.manong.hylian.client.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.HandlerInterceptor;
import xin.manong.hylian.client.config.HylianClientConfig;
import xin.manong.hylian.client.core.HylianShield;
import xin.manong.hylian.client.core.ContextManager;

/**
 * Hylian拦截器：负责安全拦截
 *
 * @author frankcl
 * @date 2023-09-05 19:34:12
 */
public class HylianInterceptor implements HandlerInterceptor {

    private final HylianShield shield;

    public HylianInterceptor(HylianClientConfig clientConfig) {
        shield = new HylianShield(clientConfig.appId, clientConfig.appSecret, clientConfig.serverURL);
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
        if (shield.shelter(httpRequest, httpResponse)) {
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
}
