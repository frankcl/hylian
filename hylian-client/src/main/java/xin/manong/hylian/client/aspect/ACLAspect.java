package xin.manong.hylian.client.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xin.manong.hylian.client.util.HTTPUtils;
import xin.manong.hylian.client.util.PermissionUtils;
import xin.manong.hylian.model.Permission;
import xin.manong.hylian.model.User;
import xin.manong.hylian.client.component.UserServiceSupport;
import xin.manong.hylian.client.core.ContextManager;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    protected UserServiceSupport userServiceSupport;

    @Pointcut("@annotation(xin.manong.hylian.client.aspect.EnableACLAspect) && execution(public * *(..))")
    public void intercept() {
    }

    /**
     * 拦截控制器方法，进行ACL鉴权
     *
     * @param joinPoint 拦截目标
     * @return 响应对象
     */
    @Around("intercept()")
    public Object aroundIntercept(ProceedingJoinPoint joinPoint) throws Throwable {
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
        return userServiceSupport.getUserPermissions(user, httpRequest);
    }
}
