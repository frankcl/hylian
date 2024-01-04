package xin.manong.security.keeper.sso.client.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xin.manong.security.keeper.common.util.HTTPUtils;
import xin.manong.security.keeper.common.util.PermissionUtils;
import xin.manong.security.keeper.model.Permission;
import xin.manong.security.keeper.model.User;
import xin.manong.security.keeper.sso.client.component.UserRolePermissionService;
import xin.manong.security.keeper.sso.client.core.ContextManager;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import java.util.*;

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

    @Resource
    protected UserRolePermissionService userRolePermissionService;

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
                throw new NotAuthorizedException("用户尚未登录");
            }
            List<Permission> permissions = getUserPermissions(user);
            if (permissions.isEmpty()) {
                logger.error("permissions are not found for user[{}]", user.id);
                throw new ForbiddenException("用户权限为空");
            }
            if (!permissionAllow(permissions)) {
                logger.error("permission are not allowed");
                throw new ForbiddenException("无权访问");
            }
            return joinPoint.proceed();
        } catch (Throwable t) {
            if (t instanceof ClientErrorException) throw (ClientErrorException) t;
            else {
                logger.error(t.getMessage(), t);
                throw new Exception(t.getMessage(), t);
            }
        }
    }

    /**
     * 判断是否权限允许
     *
     * @param permissions 权限列表
     * @return 允许返回true，否则返回false
     */
    private boolean permissionAllow(List<Permission> permissions) {
        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.
                currentRequestAttributes()).getRequest();
        if (httpRequest == null) {
            logger.error("get http servlet request failed");
            return false;
        }
        String path = HTTPUtils.getRequestPath(httpRequest);
        for (Permission permission : permissions) {
            if (PermissionUtils.match(permission.resource, path)) return true;
        }
        return false;
    }

    /**
     * 获取用户权限
     *
     * @param user 用户信息
     * @return 用户权限列表
     */
    private List<Permission> getUserPermissions(User user) {
        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.
                currentRequestAttributes()).getRequest();
        return userRolePermissionService.getUserPermissions(user, httpRequest);
    }
}
