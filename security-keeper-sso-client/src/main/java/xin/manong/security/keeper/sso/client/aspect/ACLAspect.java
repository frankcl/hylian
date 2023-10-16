package xin.manong.security.keeper.sso.client.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.security.keeper.model.Permission;
import xin.manong.security.keeper.model.User;
import xin.manong.security.keeper.sso.client.core.ContextManager;

import java.util.List;

/**
 * 访问控制切面
 *
 * @author frankcl
 * @date 2023-10-13 17:50:32
 */
@Component
@Aspect
public class ACLAspect {

    private static final Logger logger = LoggerFactory.getLogger(ACLAspect.class);

    @Pointcut("@annotation(xin.manong.security.keeper.sso.client.aspect.EnableACLAspect) && execution(public * *(..))")
    public void intercept() {
    }

    @Before("intercept()")
    public void beforeIntercept(JoinPoint joinPoint) {
    }

    @AfterReturning(value = "intercept()", returning = "response")
    public void afterReturn(JoinPoint joinPoint, Object response) {
    }

    /**
     * 拦截控制器方法，进行ACL鉴权
     *
     * @param joinPoint 拦截目标
     * @return 响应对象
     */
    @Around("intercept()")
    public Object aroundIntercept(ProceedingJoinPoint joinPoint) throws Exception {
        try {
            User user = ContextManager.getUser();
            if (user == null) {
                logger.error("user not login");
                throw new Exception("用户尚未登录");
            }
            return joinPoint.proceed();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new Exception(t.getMessage(), t);
        }
    }

    /**
     * 获取用户权限
     *
     * @return 用户权限列表
     */
    private List<Permission> getUserPermissions() {
        return null;
    }
}
