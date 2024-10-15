package xin.manong.hylian.server.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import xin.manong.hylian.client.core.ContextManager;
import xin.manong.hylian.model.AppUser;
import xin.manong.hylian.model.User;
import xin.manong.hylian.server.service.AppUserService;

import javax.annotation.Resource;
import javax.ws.rs.NotAuthorizedException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 关注应用切面
 * 注入用户关注（有权限操作）应用列表
 *
 * @author frankcl
 * @date 2024-10-23 13:04:01
 */
@Component
@Aspect
@Order(1001)
public class AppFollowAspect {

    private static final Logger logger = LoggerFactory.getLogger(AppFollowAspect.class);

    @Resource
    protected AppUserService appUserService;

    @Pointcut("@annotation(xin.manong.hylian.server.aspect.EnableAppFollowAspect) && execution(public * *(..))")
    public void intercept() {
    }

    /**
     * 拦截控制器方法，进行关注应用列表注入
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
        if (!user.superAdmin) {
            List<AppUser> appUsers = appUserService.getByUserId(user.getId());
            if (appUsers == null) appUsers = new ArrayList<>();
            ContextManager.setFollowApps(appUsers.stream().map(appUser -> appUser.appId).collect(Collectors.toList()));
        }
        try {
            return joinPoint.proceed();
        } finally {
            ContextManager.removeFollowApps();
        }
    }
}
