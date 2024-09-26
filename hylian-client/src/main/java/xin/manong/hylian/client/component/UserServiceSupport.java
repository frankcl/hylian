package xin.manong.hylian.client.component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.hylian.common.util.SessionUtils;
import xin.manong.hylian.model.Permission;
import xin.manong.hylian.model.Role;
import xin.manong.hylian.model.User;
import xin.manong.hylian.client.common.Constants;
import xin.manong.hylian.client.config.HylianClientConfig;
import xin.manong.hylian.client.core.HTTPExecutor;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestFormat;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户服务支持
 *
 * @author frankcl
 * @date 2024-01-04 16:03:31
 */
@SuppressWarnings("unchecked")
@Component
public class UserServiceSupport {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceSupport.class);

    private static final int CACHE_CAPACITY = 300;

    protected Cache<String, Long> rolesCache;
    protected Cache<String, Long> permissionsCache;
    @Resource
    protected HylianClientConfig clientConfig;

    public UserServiceSupport() {
        {
            CacheBuilder<String, Long> builder = CacheBuilder.newBuilder()
                    .concurrencyLevel(1)
                    .maximumSize(CACHE_CAPACITY)
                    .expireAfterWrite(2, TimeUnit.MINUTES)
                    .removalListener(n -> logger.info("roles are removed from cache for user[{}]", n.getKey()));
            rolesCache = builder.build();
        }
        {
            CacheBuilder<String, Long> builder = CacheBuilder.newBuilder()
                    .concurrencyLevel(1)
                    .maximumSize(CACHE_CAPACITY)
                    .expireAfterWrite(2, TimeUnit.MINUTES)
                    .removalListener(n -> logger.info("permissions are removed from cache for user[{}]", n.getKey()));
            permissionsCache = builder.build();
        }
    }

    /**
     * 获取用户角色列表
     * 优先从http session中获取，session不存在查询服务获取
     *
     * @param user 用户信息
     * @param httpRequest HTTP请求
     * @return 角色列表
     */
    public List<Role> getUserRoles(User user, HttpServletRequest httpRequest) {
        if (httpRequest == null) throw new IllegalArgumentException("http request is null");
        List<Role> roles = SessionUtils.getRoles(httpRequest);
        if (roles != null && rolesCache.getIfPresent(user.id) != null) return roles;
        roles = getUserRoles(user);
        SessionUtils.setRoles(httpRequest, roles);
        rolesCache.put(user.id, System.currentTimeMillis());
        return roles;
    }

    /**
     * 获取用户权限列表
     * 优先从http session中获取，session不存在查询服务获取
     *
     * @param user 用户信息
     * @param httpRequest HTTP请求
     * @return 权限列表
     */
    public List<Permission> getUserPermissions(User user, HttpServletRequest httpRequest) {
        if (httpRequest == null) throw new IllegalArgumentException("http request is null");
        List<Permission> permissions = SessionUtils.getPermissions(httpRequest);
        if (permissions != null && permissionsCache.getIfPresent(user.id) != null) return permissions;
        permissions = getUserPermissions(user);
        SessionUtils.setPermissions(httpRequest, permissions);
        permissionsCache.put(user.id, System.currentTimeMillis());
        return permissions;
    }

    /**
     * 获取用户角色列表
     *
     * @param user 用户信息
     * @return 角色列表
     */
    public List<Role> getUserRoles(User user) {
        String requestURL = String.format("%s%s", clientConfig.serverURL,
                Constants.SERVER_PATH_GET_APP_USER_ROLES);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Constants.PARAM_APP_ID, clientConfig.appId);
        paramMap.put(Constants.PARAM_APP_SECRET, clientConfig.appSecret);
        paramMap.put(Constants.PARAM_USER_ID, user.id);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        List<Role> roles = HTTPExecutor.executeAndUnwrap(httpRequest, List.class, Role.class);
        return roles == null ? new ArrayList<>() : roles;
    }

    /**
     * 获取用户权限列表
     *
     * @param user 用户信息
     * @return 权限列表
     */
    public List<Permission> getUserPermissions(User user) {
        List<Role> roles = getUserRoles(user);
        if (roles == null || roles.isEmpty()) return new ArrayList<>();
        List<String> roleIds = roles.stream().map(role -> role.id).collect(Collectors.toList());
        String requestURL = String.format("%s%s", clientConfig.serverURL,
                Constants.SERVER_PATH_GET_APP_ROLE_PERMISSIONS);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Constants.PARAM_ROLE_IDS, roleIds);
        requestBody.put(Constants.PARAM_APP_ID, clientConfig.appId);
        requestBody.put(Constants.PARAM_APP_SECRET, clientConfig.appSecret);
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, requestBody);
        List<Permission> permissions = HTTPExecutor.executeAndUnwrap(httpRequest, List.class, Permission.class);
        return permissions == null ? new ArrayList<>() : permissions;
    }
}
