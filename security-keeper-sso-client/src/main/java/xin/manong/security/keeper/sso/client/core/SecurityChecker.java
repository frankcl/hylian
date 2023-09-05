package xin.manong.security.keeper.sso.client.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.security.keeper.common.util.HTTPUtils;
import xin.manong.security.keeper.common.util.SessionUtils;
import xin.manong.security.keeper.model.Tenant;
import xin.manong.security.keeper.model.User;
import xin.manong.security.keeper.model.Vendor;
import xin.manong.security.keeper.model.request.RefreshTokenRequest;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestFormat;
import xin.manong.weapon.spring.web.WebResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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

    private static final String PARAM_CODE = "code";
    private static final String PARAM_TOKEN = "token";
    private static final String PARAM_APP_ID = "app_id";
    private static final String PARAM_APP_SECRET = "app_secret";
    private static final String PARAM_REDIRECT_URL = "redirect_url";

    private static final String CLIENT_PATH_LOGOUT = "/logout";
    private static final String CLIENT_PATH_EXECUTE_LOGOUT = "/sso/executeLogout";

    private static final String SERVER_PATH_LOGIN = "security/sso/login";
    private static final String SERVER_PATH_LOGOUT = "security/sso/logout";
    private static final String SERVER_PATH_APPLY_CODE = "security/auth/applyCode";
    private static final String SERVER_PATH_GET_TOKEN = "security/auth/getToken";
    private static final String SERVER_PATH_GET_USER = "security/auth/getUser";
    private static final String SERVER_PATH_GET_TENANT = "security/resource/getTenant";
    private static final String SERVER_PATH_GET_VENDOR = "security/resource/getVendor";
    private static final String SERVER_PATH_REFRESH_TOKEN = "security/auth/refreshToken";

    private String appId;
    private String appSecret;
    private String serverURL;
    private HttpClient httpClient;

    public SecurityChecker(String appId,
                           String appSecret,
                           String serverURL) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.serverURL = serverURL;
        this.httpClient = new HttpClient();
    }

    /**
     * 安全登录检测
     *
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @return 检测通过返回true，否则返回false
     * @throws IOException
     */
    public boolean check(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
        throws IOException {
        String requestPath = HTTPUtils.getRequestPath(httpRequest);
        if (requestPath.equals(CLIENT_PATH_LOGOUT)) {
            serverLogout();
            return false;
        } else if (requestPath.equals(CLIENT_PATH_EXECUTE_LOGOUT)) {
            HttpSession httpSession = httpRequest.getSession();
            if (httpSession != null) httpSession.invalidate();
            return false;
        }
        String token = SessionUtils.getToken(httpRequest);
        if (!StringUtils.isEmpty(token)) {
            if (SessionUtils.getUser(httpRequest) == null && !refreshUser(token, httpRequest, httpResponse)) return false;
            if (SessionUtils.getTenant(httpRequest) == null && !refreshTenant(token, httpRequest, httpResponse)) return false;
            if (SessionUtils.getVendor(httpRequest) == null && !refreshVendor(token, httpRequest, httpResponse)) return false;
            if (refreshToken(token, httpRequest)) return true;
        }
        String code = httpRequest.getParameter(PARAM_CODE);
        if (!StringUtils.isEmpty(code)) {
            token = getToken(code);
            if (StringUtils.isEmpty(token)) {
                redirectLogin(httpRequest, httpResponse);
                return false;
            }
            if (!refreshUser(token, httpRequest, httpResponse) ||
                    !refreshTenant(token, httpRequest, httpResponse) ||
                    !refreshVendor(token, httpRequest, httpResponse)) return false;
            SessionUtils.setToken(httpRequest, token);
            redirectRequestURLWithoutCode(httpRequest, httpResponse);
            return false;
        }
        String requestURL = HTTPUtils.getRequestURL(httpRequest);
        applyCode(requestURL);
        return false;
    }

    /**
     * 重定向到请求URL并去除code参数
     *
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @throws IOException
     */
    private void redirectRequestURLWithoutCode(HttpServletRequest httpRequest,
                                               HttpServletResponse httpResponse) throws IOException {
        String requestURL = HTTPUtils.getRequestURL(httpRequest);
        Set<String> queryKeys = new HashSet<>();
        queryKeys.add(PARAM_CODE);
        requestURL = HTTPUtils.removeQueries(requestURL, queryKeys);
        httpResponse.sendRedirect(requestURL);
    }

    /**
     * 重定向到登录页
     *
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @throws IOException
     */
    private void redirectLogin(HttpServletRequest httpRequest,
                               HttpServletResponse httpResponse) throws IOException {
        String requestURL = HTTPUtils.getRequestURL(httpRequest);
        httpResponse.sendRedirect(String.format("%s%s?%s=%s",
                serverURL, SERVER_PATH_LOGIN, PARAM_REDIRECT_URL,
                URLEncoder.encode(requestURL == null ? "" : requestURL, "UTF-8")));
    }

    /**
     * 申请安全码
     *
     * @param redirectURL 重定向URL
     */
    private void applyCode(String redirectURL) {
        String requestURL = String.format("%s%s", serverURL, SERVER_PATH_APPLY_CODE);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(PARAM_REDIRECT_URL, redirectURL);
        paramMap.put(PARAM_APP_ID, appId);
        paramMap.put(PARAM_APP_SECRET, appSecret);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        WebResponse response = execute(httpRequest, null);
        if (response == null) logger.warn("apply code failed");
    }

    /**
     * 根据安全码获取token
     *
     * @param code 安全码
     * @return 成功返回token，否则返回null
     */
    private String getToken(String code) {
        String requestURL = String.format("%s%s", serverURL, SERVER_PATH_GET_TOKEN);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(PARAM_CODE, code);
        paramMap.put(PARAM_APP_ID, appId);
        paramMap.put(PARAM_APP_SECRET, appSecret);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        WebResponse<String> response = execute(httpRequest, new TypeReference<WebResponse<String>>() {});
        return response == null ? null : response.data;
    }

    /**
     * 根据token获取用户信息
     *
     * @param token
     * @return 成功返回用户信息，否则返回null
     */
    private User getUser(String token) {
        String requestURL = String.format("%s%s", serverURL, SERVER_PATH_GET_USER);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(PARAM_TOKEN, token);
        paramMap.put(PARAM_APP_ID, appId);
        paramMap.put(PARAM_APP_SECRET, appSecret);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        WebResponse<User> response = execute(httpRequest, new TypeReference<WebResponse<User>>() {});
        return response == null ? null : response.data;
    }

    /**
     * 根据token获取租户信息
     *
     * @param token
     * @return 成功返回租户信息，否则返回null
     */
    private Tenant getTenant(String token) {
        String requestURL = String.format("%s%s", serverURL, SERVER_PATH_GET_TENANT);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(PARAM_TOKEN, token);
        paramMap.put(PARAM_APP_ID, appId);
        paramMap.put(PARAM_APP_SECRET, appSecret);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        WebResponse<Tenant> response = execute(httpRequest, new TypeReference<WebResponse<Tenant>>() {});
        return response == null ? null : response.data;
    }

    /**
     * 根据token获取供应商信息
     *
     * @param token
     * @return 成功返回供应商信息，否则返回null
     */
    private Vendor getVendor(String token) {
        String requestURL = String.format("%s%s", serverURL, SERVER_PATH_GET_VENDOR);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(PARAM_TOKEN, token);
        paramMap.put(PARAM_APP_ID, appId);
        paramMap.put(PARAM_APP_SECRET, appSecret);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        WebResponse<Vendor> response = execute(httpRequest, new TypeReference<WebResponse<Vendor>>() {});
        return response == null ? null : response.data;
    }

    /**
     * 刷新token
     *
     * @param token 原token
     * @param httpServletRequest HTTP请求
     * @return 刷新成功返回true，否则返回false
     */
    private boolean refreshToken(String token, HttpServletRequest httpServletRequest) {
        String requestURL = String.format("%s%s", serverURL, SERVER_PATH_REFRESH_TOKEN);
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.appId = appId;
        request.appSecret = appSecret;
        request.token = token;
        Map<String, Object> body = JSON.parseObject(JSON.toJSONString(request));
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, body);
        WebResponse<String> response = execute(httpRequest, new TypeReference<WebResponse<String>>() {});
        if (response == null || StringUtils.isEmpty(response.data)) return false;
        SessionUtils.setToken(httpServletRequest, response.data);
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
            redirectLogin(httpRequest, httpResponse);
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
            redirectLogin(httpRequest, httpResponse);
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
            redirectLogin(httpRequest, httpResponse);
            return false;
        }
        SessionUtils.setVendor(httpRequest, vendor);
        return true;
    }

    /**
     * SSO服务端注销
     */
    private void serverLogout() {
        String requestURL = String.format("%s%s", serverURL, SERVER_PATH_LOGOUT);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(PARAM_APP_ID, appId);
        paramMap.put(PARAM_APP_SECRET, appSecret);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        WebResponse response = execute(httpRequest, null);
        if (response == null) logger.warn("security keeper server sso logout failed");
    }

    /**
     * 执行HTTP请求
     *
     * @param httpRequest HTTP请求
     * @param typeReference 结果数据类型
     * @return 成功返回结果，否则返回null
     * @param <T>
     */
    private <T> WebResponse<T> execute(HttpRequest httpRequest, TypeReference<WebResponse<T>> typeReference) {
        Response httpResponse = httpClient.execute(httpRequest);
        try {
            if (httpResponse == null || !httpResponse.isSuccessful() || httpResponse.code() != 200) {
                logger.error("request failed for url[{}]", httpRequest.requestURL);
                return null;
            }
            WebResponse<T> response;
            if (typeReference == null) response = JSON.parseObject(httpResponse.body().string(), WebResponse.class);
            else response = JSON.parseObject(httpResponse.body().string(), typeReference);
            if (!response.status) {
                logger.error("request failed for url[{}], message[{}]", httpRequest.requestURL, response.message);
                return null;
            }
            return response;
        } catch (Exception e) {
            logger.error("request exception for url[{}], cause[{}]", httpRequest.requestURL, e.getMessage());
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (httpResponse != null) httpResponse.close();
        }
    }
}
