package xin.manong.hylian.server.aspect;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.NotAuthorizedException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xin.manong.hylian.server.config.ServerConfig;
import xin.manong.hylian.client.util.CookieUtils;
import xin.manong.hylian.server.common.Constants;

/**
 * cookie清理切面
 *
 * @author frankcl
 * @date 2024-09-23 13:04:01
 */
@Component
@Aspect
@Order(2000)
public class CookieSweepAspect {

    @Resource
    private ServerConfig serverConfig;

    @Pointcut("execution(public * xin.manong.hylian.server.controller.*.*(..))")
    public void intercept() {
    }

    /**
     * 如果抛出异常NotAcceptableException清理cookie
     *
     * @param joinPoint 拦截点
     * @param throwable 异常
     */
    @AfterThrowing(pointcut = "intercept()", throwing = "throwable")
    public void afterThrowing(JoinPoint joinPoint, Throwable throwable) {
        if (throwable instanceof NotAuthorizedException) {
            HttpServletResponse httpResponse = ((ServletRequestAttributes) RequestContextHolder.
                    currentRequestAttributes()).getResponse();
            CookieUtils.removeCookie(Constants.COOKIE_TOKEN, "/", serverConfig.domain, httpResponse);
            CookieUtils.removeCookie(Constants.COOKIE_TICKET, "/", serverConfig.domain, httpResponse);
        }
    }
}
