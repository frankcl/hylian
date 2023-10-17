package xin.manong.security.keeper.sso.client.aspect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import xin.manong.security.keeper.common.util.SessionUtils;
import xin.manong.security.keeper.model.Permission;
import xin.manong.security.keeper.model.Role;
import xin.manong.security.keeper.model.User;
import xin.manong.security.keeper.sso.client.common.Constants;
import xin.manong.security.keeper.sso.client.config.AppClientConfig;
import xin.manong.security.keeper.sso.client.core.ContextManager;
import xin.manong.security.keeper.sso.client.core.HTTPExecutor;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestFormat;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    private static final int CACHE_CAPACITY = 300;

    protected Cache<String, Long> permissionsCache;

    @Resource
    protected AppClientConfig appClientConfig;

    public ACLAspect() {
        CacheBuilder<String, Long> builder = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .maximumSize(CACHE_CAPACITY)
                .expireAfterWrite(2, TimeUnit.MINUTES)
                .removalListener(n -> logger.info("permissions are removed from cache for user[{}]", n.getKey()));
        permissionsCache = builder.build();
    }

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
            List<Permission> permissions = getUserPermissions(user);
            if (permissions.isEmpty()) {
                logger.error("permissions are not found for user[{}]", user.id);
                throw new Exception("用户权限为空");
            }
            if (!permissionAllow(permissions)) {
                logger.error("permission are not allowed");
                throw new Exception("无权访问");
            }
            return joinPoint.proceed();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new Exception(t.getMessage(), t);
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
        List<Permission> permissions = SessionUtils.getPermissions(httpRequest);
        if (permissions != null && permissionsCache.getIfPresent(user.id) != null) return permissions;
        List<Role> roles = getAppUserRoles(user);
        if (roles.isEmpty()) return new ArrayList<>();
        permissions = getUserPermissions(roles, user);
        SessionUtils.setPermissions(httpRequest, permissions);
        permissionsCache.put(user.id, System.currentTimeMillis());
        return permissions;
    }

    /**
     * 获取应用用户角色列表
     *
     * @param user 用户信息
     * @return 角色列表
     */
    private List<Role> getAppUserRoles(User user) {
        String requestURL = String.format("%s%s", appClientConfig.serverURL,
                Constants.SERVER_PATH_GET_APP_USER_ROLES);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Constants.PARAM_APP_ID, appClientConfig.appId);
        paramMap.put(Constants.PARAM_USER_ID, user.id);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        List<Role> roles = HTTPExecutor.execute(httpRequest, List.class);
        if (roles == null || roles.isEmpty()) {
            logger.error("get roles failed or roles not existed for user[{}] and app[{}]",
                    user.id, appClientConfig.appId);
            return new ArrayList<>();
        }
        return roles;
    }

    /**
     * 获取用户权限列表
     *
     * @param roles 角色列表
     * @param user 用户信息
     * @return 权限列表
     */
    private List<Permission> getUserPermissions(List<Role> roles, User user) {
        List<String> roleIds = roles.stream().map(role -> role.id).collect(Collectors.toList());
        String requestURL = String.format("%s%s", appClientConfig.serverURL,
                Constants.SERVER_PATH_GET_ROLE_PERMISSIONS);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Constants.PARAM_ROLE_IDS, roleIds);
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, requestBody);
        List<Permission> permissions = HTTPExecutor.execute(httpRequest, List.class);
        if (permissions == null || permissions.isEmpty()) {
            logger.error("get permissions failed or permissions not existed for user[{}] and app[{}]",
                    user.id, appClientConfig.appId);
            return new ArrayList<>();
        }
        return permissions;
    }
}
