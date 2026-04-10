package xin.manong.hylian.client.component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xin.manong.hylian.client.core.HylianClient;
import xin.manong.hylian.client.util.SessionUtils;
import xin.manong.hylian.model.Permission;
import xin.manong.hylian.model.Role;
import xin.manong.hylian.model.User;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
    protected HylianClient hylianClient;

    @Autowired
    public UserServiceSupport(HylianClient hylianClient) {
        this.hylianClient = hylianClient;
        {
            CacheBuilder<String, Long> builder = CacheBuilder.newBuilder()
                    .concurrencyLevel(1)
                    .maximumSize(CACHE_CAPACITY)
                    .expireAfterWrite(30, TimeUnit.SECONDS)
                    .removalListener(n -> logger.info("Roles are removed from cache for user:{}", n.getKey()));
            rolesCache = builder.build();
        }
        {
            CacheBuilder<String, Long> builder = CacheBuilder.newBuilder()
                    .concurrencyLevel(1)
                    .maximumSize(CACHE_CAPACITY)
                    .expireAfterWrite(30, TimeUnit.SECONDS)
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
        roles = hylianClient.getUserRoles(user);
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
        permissions = hylianClient.getUserPermissions(user);
        SessionUtils.setPermissions(httpRequest, permissions);
        permissionsCache.put(user.id, System.currentTimeMillis());
        return permissions;
    }

    /**
     * 获取所有用户信息
     *
     * @return 用户列表
     */
    public List<User> getUsers() {
        return hylianClient.getUsers();
    }

    /**
     * 批量获取用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    public List<User> batchGetUsers(List<String> userIds) {
        return hylianClient.batchGetUsers(userIds);
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    public User getUser(String id) {
        return hylianClient.getUserById(id);
    }

    /**
     * 是否为应用管理员
     *
     * @param user 用户信息
     * @return 应用管理员返回true，否则返回false
     */
    public boolean isAppAdmin(User user) {
        return hylianClient.isAppAdmin(user);
    }
}
