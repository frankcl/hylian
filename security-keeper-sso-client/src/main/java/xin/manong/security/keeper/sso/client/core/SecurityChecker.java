package xin.manong.security.keeper.sso.client.core;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.security.keeper.common.util.HTTPUtils;
import xin.manong.security.keeper.common.util.SessionUtils;
import xin.manong.security.keeper.model.Tenant;
import xin.manong.security.keeper.model.User;
import xin.manong.security.keeper.model.Vendor;
import xin.manong.security.keeper.model.request.RefreshTokenRequest;
import xin.manong.security.keeper.sso.client.common.Constants;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

/**
 * 安全登录检测
 *
 * @author frankcl
 * @date 2023-09-04 14:26:59
 */
public class SecurityChecker {

    private static final Logger logger = LoggerFactory.getLogger(SecurityChecker.class);

    private String appId;
    private String appSecret;
    private String serverURL;

    public SecurityChecker(String appId,
                           String appSecret,
                           String serverURL) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.serverURL = serverURL;
    }

    /**
     * 安全登录检测
     *
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @return 检测通过返回true，否则返回false
     * @throws IOException
     */
    public boolean check(HttpServletRequest httpRequest,
                         HttpServletResponse httpResponse) throws IOException {
        String requestPath = HTTPUtils.getRequestPath(httpRequest);
        if (requestPath.equals(Constants.CLIENT_PATH_LOGOUT)) {
            redirectServerLogout(httpRequest, httpResponse);
            return false;
        } else if (requestPath.equals(Constants.CLIENT_PATH_LOGOUT_DESTROY)) {
            Map<String, String> queryMap = HTTPUtils.getRequestQueryMap(httpRequest);
            String sessionId = queryMap.getOrDefault(Constants.PARAM_SESSION_ID, null);
            SessionManager.invalidate(sessionId);
            return false;
        }
        if (checkToken(httpRequest)) {
            String token = SessionUtils.getToken(httpRequest);
            if (SessionUtils.getUser(httpRequest) == null && !refreshUser(token, httpRequest, httpResponse)) return false;
            if (SessionUtils.getTenant(httpRequest) == null && !refreshTenant(token, httpRequest, httpResponse)) return false;
            if (SessionUtils.getVendor(httpRequest) == null && !refreshVendor(token, httpRequest, httpResponse)) return false;
            if (refreshToken(token, httpRequest)) return true;
        }
        SessionUtils.removeResources(httpRequest);
        String code = httpRequest.getParameter(Constants.PARAM_CODE);
        if (!StringUtils.isEmpty(code)) {
            String token = acquireToken(code, httpRequest);
            if (StringUtils.isEmpty(token)) {
                redirectServerLogin(httpRequest, httpResponse);
                return false;
            }
            if (!refreshUser(token, httpRequest, httpResponse) ||
                    !refreshTenant(token, httpRequest, httpResponse) ||
                    !refreshVendor(token, httpRequest, httpResponse)) return false;
            SessionUtils.setToken(httpRequest, token);
            redirectRequestURLWithoutCode(httpRequest, httpResponse);
            return false;
        }
        redirectServerApplyCode(httpRequest, httpResponse);
        return false;
    }

    /**
     * 重定向到去除code参数的请求URL
     *
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @throws IOException
     */
    private void redirectRequestURLWithoutCode(HttpServletRequest httpRequest,
                                               HttpServletResponse httpResponse) throws IOException {
        String requestURL = HTTPUtils.getRequestURL(httpRequest);
        Set<String> queryKeys = new HashSet<>();
        queryKeys.add(Constants.PARAM_CODE);
        requestURL = HTTPUtils.removeQueries(requestURL, queryKeys);
        httpResponse.sendRedirect(requestURL);
    }

    /**
     * 重定向服务端申请安全码URL
     *
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @throws IOException
     */
    private void redirectServerApplyCode(HttpServletRequest httpRequest,
                                         HttpServletResponse httpResponse) throws IOException {
        String redirectURL = HTTPUtils.getRequestURL(httpRequest);
        httpResponse.sendRedirect(String.format("%s%s?%s=%s&%s=%s&%s=%s", serverURL, Constants.SERVER_PATH_APPLY_CODE,
                Constants.PARAM_APP_ID, appId, Constants.PARAM_APP_SECRET, appSecret, Constants.PARAM_REDIRECT_URL,
                URLEncoder.encode(redirectURL, Constants.CHARSET_UTF8)));
    }

    /**
     * 重定向服务端登录页
     *
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @throws IOException
     */
    private void redirectServerLogin(HttpServletRequest httpRequest,
                                     HttpServletResponse httpResponse) throws IOException {
        SessionUtils.removeResources(httpRequest);
        String requestURL = HTTPUtils.getRequestURL(httpRequest);
        httpResponse.sendRedirect(String.format("%s%s?%s=%s",
                serverURL, Constants.SERVER_PATH_LOGIN, Constants.PARAM_REDIRECT_URL,
                URLEncoder.encode(requestURL == null ? "" : requestURL, Constants.CHARSET_UTF8)));
    }

    /**
     * 重定向服务端注销页
     *
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     */
    private void redirectServerLogout(HttpServletRequest httpRequest,
                                      HttpServletResponse httpResponse) throws IOException {
        httpResponse.sendRedirect(String.format("%s%s?%s=%s&%s=%s&%s=%s", serverURL, Constants.SERVER_PATH_LOGOUT,
                Constants.PARAM_APP_ID, appId, Constants.PARAM_APP_SECRET, appSecret, Constants.PARAM_REDIRECT_URL,
                URLEncoder.encode(HTTPUtils.getRequestRootURL(httpRequest), Constants.CHARSET_UTF8)));
    }

    /**
     * 检测token
     *
     * @param httpRequest HTTP请求
     * @return 有效返回true，否则返回false
     */
    private boolean checkToken(HttpServletRequest httpRequest) {
        String token = SessionUtils.getToken(httpRequest);
        if (StringUtils.isEmpty(token)) return false;
        String requestURL = String.format("%s%s", serverURL, Constants.SERVER_PATH_CHECK_TOKEN);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Constants.PARAM_TOKEN, token);
        paramMap.put(Constants.PARAM_APP_ID, appId);
        paramMap.put(Constants.PARAM_APP_SECRET, appSecret);
        HttpRequest request = HttpRequest.buildGetRequest(requestURL, paramMap);
        Boolean valid = HTTPExecutor.execute(request, Boolean.class);
        return valid == null ? false : valid;
    }

    /**
     * 根据安全码获取token
     *
     * @param code 安全码
     * @param httpRequest HTTP请求
     * @return 成功返回token，否则返回null
     */
    private String acquireToken(String code, HttpServletRequest httpRequest) {
        String requestURL = String.format("%s%s", serverURL, Constants.SERVER_PATH_ACQUIRE_TOKEN);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Constants.PARAM_CODE, code);
        paramMap.put(Constants.PARAM_APP_ID, appId);
        paramMap.put(Constants.PARAM_APP_SECRET, appSecret);
        paramMap.put(Constants.PARAM_SESSION_ID, SessionUtils.getSessionID(httpRequest));
        paramMap.put(Constants.PARAM_LOGOUT_URL, String.format("%s%s",
                HTTPUtils.getRequestRootURL(httpRequest), Constants.CLIENT_PATH_LOGOUT_DESTROY));
        HttpRequest request = HttpRequest.buildGetRequest(requestURL, paramMap);
        return HTTPExecutor.execute(request, String.class);
    }

    /**
     * 根据token获取用户信息
     *
     * @param token
     * @return 成功返回用户信息，否则返回null
     */
    private User getUser(String token) {
        String requestURL = String.format("%s%s", serverURL, Constants.SERVER_PATH_GET_USER);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Constants.PARAM_TOKEN, token);
        paramMap.put(Constants.PARAM_APP_ID, appId);
        paramMap.put(Constants.PARAM_APP_SECRET, appSecret);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        return HTTPExecutor.execute(httpRequest, User.class);
    }

    /**
     * 根据token获取租户信息
     *
     * @param token
     * @return 成功返回租户信息，否则返回null
     */
    private Tenant getTenant(String token) {
        String requestURL = String.format("%s%s", serverURL, Constants.SERVER_PATH_GET_TENANT);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Constants.PARAM_TOKEN, token);
        paramMap.put(Constants.PARAM_APP_ID, appId);
        paramMap.put(Constants.PARAM_APP_SECRET, appSecret);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        return HTTPExecutor.execute(httpRequest, Tenant.class);
    }

    /**
     * 根据token获取供应商信息
     *
     * @param token
     * @return 成功返回供应商信息，否则返回null
     */
    private Vendor getVendor(String token) {
        String requestURL = String.format("%s%s", serverURL, Constants.SERVER_PATH_GET_VENDOR);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Constants.PARAM_TOKEN, token);
        paramMap.put(Constants.PARAM_APP_ID, appId);
        paramMap.put(Constants.PARAM_APP_SECRET, appSecret);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        return HTTPExecutor.execute(httpRequest, Vendor.class);
    }

    /**
     * 刷新token
     *
     * @param token 原token
     * @param httpServletRequest HTTP请求
     * @return 刷新成功返回true，否则返回false
     */
    private boolean refreshToken(String token, HttpServletRequest httpServletRequest) {
        String requestURL = String.format("%s%s", serverURL, Constants.SERVER_PATH_REFRESH_TOKEN);
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.appId = appId;
        request.appSecret = appSecret;
        request.token = token;
        Map<String, Object> body = JSON.parseObject(JSON.toJSONString(request));
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, body);
        String newToken = HTTPExecutor.execute(httpRequest, String.class);
        if (StringUtils.isEmpty(newToken)) return false;
        SessionUtils.setToken(httpServletRequest, newToken);
        return true;
    }

    /**
     * 刷新session用户信息
     *
     * @param token
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @return 成功返回true，否则返回false
     * @throws IOException
     */
    private boolean refreshUser(String token, HttpServletRequest httpRequest,
                                HttpServletResponse httpResponse) throws IOException {
        User user = getUser(token);
        if (user == null) {
            logger.error("get user failed for token[{}]", token);
            redirectServerLogin(httpRequest, httpResponse);
            return false;
        }
        SessionUtils.setUser(httpRequest, user);
        return true;
    }

    /**
     * 刷新session租户信息
     *
     * @param token
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @return 成功返回true，否则返回false
     * @throws IOException
     */
    private boolean refreshTenant(String token, HttpServletRequest httpRequest,
                                  HttpServletResponse httpResponse) throws IOException {
        Tenant tenant = getTenant(token);
        if (tenant == null) {
            logger.error("get tenant failed for token[{}]", token);
            redirectServerLogin(httpRequest, httpResponse);
            return false;
        }
        SessionUtils.setTenant(httpRequest, tenant);
        return true;
    }

    /**
     * 刷新session供应商信息
     *
     * @param token
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @return 成功返回true，否则返回false
     * @throws IOException
     */
    private boolean refreshVendor(String token, HttpServletRequest httpRequest,
                                  HttpServletResponse httpResponse) throws IOException {
        Vendor vendor = getVendor(token);
        if (vendor == null) {
            logger.error("get vendor failed for token[{}]", token);
            redirectServerLogin(httpRequest, httpResponse);
            return false;
        }
        SessionUtils.setVendor(httpRequest, vendor);
        return true;
    }
}
