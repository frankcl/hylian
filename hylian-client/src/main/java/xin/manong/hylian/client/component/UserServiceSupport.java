package xin.manong.hylian.client.component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.hylian.client.util.SessionUtils;
import xin.manong.hylian.model.Permission;
import xin.manong.hylian.model.Role;
import xin.manong.hylian.model.User;
import xin.manong.hylian.client.common.Constants;
import xin.manong.hylian.client.config.HylianClientConfig;
import xin.manong.hylian.client.core.HTTPExecutor;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestFormat;

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
                    .removalListener(n -> logger.info("Roles are removed from cache for user:{}", n.getKey()));
            rolesCache = builder.build();
        }
        {
            CacheBuilder<String, Long> builder = CacheBuilder.newBuilder()
                    .concurrencyLevel(1)
                    .maximumSize(CACHE_CAPACITY)
                    .expireAfterWrite(2, TimeUnit.MINUTES)
                    .removalListener(n -> logger.info("Permissions are removed from cache for user:{}", n.getKey()));
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
        List<Permission> permissions = SessionUtils.getPermissions(httpRequest);
        if (permissions != null && permissionsCache.getIfPresent(user.id) != null) return permissions;
        permissions = getUserPermissions(user);
        SessionUtils.setPermissions(httpRequest, permissions);
        permissionsCache.put(user.id, System.currentTimeMillis());
        return permissions;
    }

    /**
     * 是否为应用管理员
     *
     * @param user 用户信息
     * @return 应用管理员返回true，否则返回false
     */
    public boolean isAppAdmin(User user) {
        String requestURL = String.format("%s%s", clientConfig.serverURL,
                Constants.SERVER_PATH_IS_APP_ADMIN);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Constants.PARAM_USER_ID, user.id);
        paramMap.put(Constants.PARAM_APP_ID, clientConfig.appId);
        paramMap.put(Constants.PARAM_APP_SECRET, clientConfig.appSecret);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        Boolean success = HTTPExecutor.executeAndUnwrap(httpRequest, Boolean.class);
        return success != null && success;
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
        List<Role> roles = HTTPExecutor.executeAndUnwrapList(httpRequest, Role.class);
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
        List<Permission> permissions = HTTPExecutor.executeAndUnwrapList(httpRequest, Permission.class);
        return permissions == null ? new ArrayList<>() : permissions;
    }

    /**
     * 获取所有用户信息
     *
     * @return 用户列表
     */
    public List<User> getAllUsers() {
        String requestURL = String.format("%s%s", clientConfig.serverURL,
                Constants.SERVER_PATH_GET_ALL_USERS);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Constants.PARAM_APP_ID, clientConfig.appId);
        requestBody.put(Constants.PARAM_APP_SECRET, clientConfig.appSecret);
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, requestBody);
        List<User> users = HTTPExecutor.executeAndUnwrapList(httpRequest, User.class);
        return users == null ? new ArrayList<>() : users;
    }
}
