package xin.manong.hylian.client.core;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import xin.manong.hylian.client.common.Constants;
import xin.manong.hylian.client.config.HylianClientConfig;
import xin.manong.hylian.client.util.SessionUtils;
import xin.manong.hylian.model.Permission;
import xin.manong.hylian.model.Role;
import xin.manong.hylian.model.Tenant;
import xin.manong.hylian.model.User;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestFormat;
import xin.manong.weapon.jersey.WebResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * hylian客户端
 *
 * @author frankcl
 * @date 2026-01-28 09:29:20
 */
@Getter
public class HylianClient {

    private final HylianClientConfig config;

    public HylianClient(HylianClientConfig config) {
        assert config != null;
        config.check();
        this.config = config;
    }

    /**
     * 强制注销
     *
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @throws IOException I/O异常
     */
    public void forceLogout(HttpServletRequest httpRequest,
                            HttpServletResponse httpResponse) throws IOException {
        httpResponse.sendRedirect(String.format("%s%s?%s=%s&%s=%s", config.serverURL,
                Constants.SERVER_PATH_LOGOUT, Constants.PARAM_APP_ID, config.appId,
                Constants.PARAM_APP_SECRET, config.appSecret));
        SessionManager.invalidate(httpRequest.getSession().getId());
    }

    /**
     * 获取用户角色列表
     *
     * @param user 用户信息
     * @return 角色列表
     */
    public List<Role> getUserRoles(User user) {
        String requestURL = String.format("%s%s", config.serverURL, Constants.SERVER_PATH_GET_APP_USER_ROLES);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Constants.PARAM_APP_ID, config.appId);
        paramMap.put(Constants.PARAM_APP_SECRET, config.appSecret);
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
        return getRolePermissions(roleIds);
    }

    /**
     * 获取角色对应权限列表
     *
     * @param roleIds 角色列表
     * @return 权限列表
     */
    public List<Permission> getRolePermissions(List<String> roleIds) {
        String requestURL = String.format("%s%s", config.serverURL,
                Constants.SERVER_PATH_GET_APP_ROLE_PERMISSIONS);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Constants.PARAM_ROLE_IDS, roleIds);
        requestBody.put(Constants.PARAM_APP_ID, config.appId);
        requestBody.put(Constants.PARAM_APP_SECRET, config.appSecret);
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, requestBody);
        List<Permission> permissions = HTTPExecutor.executeAndUnwrapList(httpRequest, Permission.class);
        return permissions == null ? new ArrayList<>() : permissions;
    }

    /**
     * 获取所有用户信息
     *
     * @return 用户列表
     */
    public List<User> getUsers() {
        String requestURL = String.format("%s%s", config.serverURL,
                Constants.SERVER_PATH_GET_USERS);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Constants.PARAM_APP_ID, config.appId);
        requestBody.put(Constants.PARAM_APP_SECRET, config.appSecret);
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, requestBody);
        List<User> users = HTTPExecutor.executeAndUnwrapList(httpRequest, User.class);
        return users == null ? new ArrayList<>() : users;
    }

    /**
     * 批量获取用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    public List<User> batchGetUsers(List<String> userIds) {
        String requestURL = String.format("%s%s", config.serverURL,
                Constants.SERVER_PATH_BATCH_GET_USERS);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Constants.PARAM_APP_ID, config.appId);
        requestBody.put(Constants.PARAM_APP_SECRET, config.appSecret);
        requestBody.put(Constants.PARAM_USER_IDS, userIds);
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, requestBody);
        List<User> users = HTTPExecutor.executeAndUnwrapList(httpRequest, User.class);
        return users == null ? new ArrayList<>() : users;
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 成功返回用户信息，否则返回null
     */
    public User getUserById(String userId) {
        String requestURL = String.format("%s%s", config.serverURL,
                Constants.SERVER_PATH_GET_USER_BY_ID);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Constants.PARAM_APP_ID, config.appId);
        requestBody.put(Constants.PARAM_APP_SECRET, config.appSecret);
        requestBody.put(Constants.PARAM_USER_ID, userId);
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, requestBody);
        return HTTPExecutor.executeAndUnwrap(httpRequest, User.class);
    }

    /**
     * 是否为应用管理员
     *
     * @param user 用户信息
     * @return 应用管理员返回true，否则返回false
     */
    public boolean isAppAdmin(User user) {
        String requestURL = String.format("%s%s", config.serverURL, Constants.SERVER_PATH_IS_APP_ADMIN);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Constants.PARAM_USER_ID, user.id);
        paramMap.put(Constants.PARAM_APP_ID, config.appId);
        paramMap.put(Constants.PARAM_APP_SECRET, config.appSecret);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        Boolean success = HTTPExecutor.executeAndUnwrap(httpRequest, Boolean.class);
        return success != null && success;
    }

    /**
     * 根据token向服务端获取用户信息
     *
     * @param token 令牌
     * @return 成功返回用户信息，否则返回null
     */
    User getUser(String token) {
        String requestURL = String.format("%s%s", config.serverURL, Constants.SERVER_PATH_GET_USER);
        Map<String, Object> paramMap = buildTokenRequest(token);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        WebResponse<User> response = HTTPExecutor.execute(httpRequest, User.class);
        if (response == null || !response.status) return null;
        return response.data;
    }

    /**
     * 根据token向服务端获取租户信息
     *
     * @param token 令牌
     * @return 成功返回租户信息，否则返回null
     */
    Tenant getTenant(String token) {
        String requestURL = String.format("%s%s", config.serverURL, Constants.SERVER_PATH_GET_TENANT);
        Map<String, Object> paramMap = buildTokenRequest(token);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        WebResponse<Tenant> response = HTTPExecutor.execute(httpRequest, Tenant.class);
        if (response == null || !response.status) return null;
        return response.data;
    }

    /**
     * 刷新token
     *
     * @param token 当前token
     * @return 成功返回新token，否则返回null
     */
    String refreshToken(String token) {
        String requestURL = String.format("%s%s", config.serverURL, Constants.SERVER_PATH_REFRESH_TOKEN);
        Map<String, Object> requestBody = buildTokenRequest(token);
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, requestBody);
        return HTTPExecutor.executeAndUnwrap(httpRequest, String.class);
    }

    /**
     * 根据code获取token
     *
     * @param code 验证code
     * @param httpRequest HTTP请求
     * @return 成功返回token，否则返回null
     */
    String acquireToken(String code, HttpServletRequest httpRequest) {
        String requestURL = String.format("%s%s", config.serverURL, Constants.SERVER_PATH_ACQUIRE_TOKEN);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Constants.PARAM_CODE, code);
        paramMap.put(Constants.PARAM_APP_ID, config.appId);
        paramMap.put(Constants.PARAM_APP_SECRET, config.appSecret);
        paramMap.put(Constants.PARAM_SESSION_ID, SessionUtils.getSessionID(httpRequest));
        HttpRequest request = HttpRequest.buildGetRequest(requestURL, paramMap);
        return HTTPExecutor.executeAndUnwrap(request, String.class);
    }

    /**
     * 移除会话活动记录
     *
     * @param sessionId 会话ID
     * @return 成功返回true，否则返回false
     */
    boolean removeActivity(String sessionId) {
        String requestURL = String.format("%s%s", config.serverURL, Constants.SERVER_PATH_REMOVE_ACTIVITY);
        Map<String, Object> body = new HashMap<>();
        body.put(Constants.PARAM_SESSION_ID, sessionId);
        body.put(Constants.PARAM_APP_ID, config.appId);
        body.put(Constants.PARAM_APP_SECRET, config.appSecret);
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, body);
        Boolean success = HTTPExecutor.executeAndUnwrap(httpRequest, Boolean.class);
        return success != null && success;
    }

    /**
     * 构建token请求
     *
     * @param token 令牌
     * @return token请求
     */
    private Map<String, Object> buildTokenRequest(String token) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Constants.PARAM_TOKEN, token);
        paramMap.put(Constants.PARAM_APP_ID, config.appId);
        paramMap.put(Constants.PARAM_APP_SECRET, config.appSecret);
        return paramMap;
    }
}
