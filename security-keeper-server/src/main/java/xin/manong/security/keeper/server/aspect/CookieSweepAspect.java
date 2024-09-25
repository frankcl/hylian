package xin.manong.security.keeper.server.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xin.manong.security.keeper.common.util.CookieUtils;
import xin.manong.security.keeper.server.common.Constants;

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
public class CookieSweepAspect {

    @Pointcut("execution(public * xin.manong.security.keeper.server.controller.*.*(..))")
    public void intercept() {
    }

    @Before("intercept()")
    public void beforeIntercept(JoinPoint joinPoint) {
    }

    @AfterReturning(value = "intercept()", returning = "response")
    public void afterReturn(JoinPoint joinPoint, Object response) {
    }

    /**
     * 拦截控制器方法，进行cookie清理
     *
     * @param joinPoint 拦截目标
     * @return 响应对象
     */
    @Around("intercept()")
    public Object aroundIntercept(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (NotAuthorizedException e) {
            HttpServletResponse httpResponse = ((ServletRequestAttributes) RequestContextHolder.
                    currentRequestAttributes()).getResponse();
            CookieUtils.removeCookie(Constants.COOKIE_TOKEN, "/", httpResponse);
            CookieUtils.removeCookie(Constants.COOKIE_TICKET, "/", httpResponse);
            throw e;
        }
    }
}
