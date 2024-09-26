package xin.manong.hylian.server.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xin.manong.hylian.common.util.CookieUtils;
import xin.manong.hylian.server.common.Constants;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;

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
        if (throwable instanceof NotAcceptableException) {
            HttpServletResponse httpResponse = ((ServletRequestAttributes) RequestContextHolder.
                    currentRequestAttributes()).getResponse();
            CookieUtils.removeCookie(Constants.COOKIE_TOKEN, "/", httpResponse);
            CookieUtils.removeCookie(Constants.COOKIE_TICKET, "/", httpResponse);
        }
    }
}
