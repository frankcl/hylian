package xin.manong.hylian.client.core;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.hylian.client.util.CookieUtils;
import xin.manong.hylian.client.util.HTTPUtils;
import xin.manong.hylian.client.util.SessionUtils;
import xin.manong.hylian.model.Tenant;
import xin.manong.hylian.model.User;
import xin.manong.hylian.client.common.Constants;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestFormat;
import xin.manong.weapon.jersey.WebResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Hylian盾：负责安全检测和登录认证
 *
 * @author frankcl
 * @date 2023-09-04 14:26:59
 */
public class HylianShield {

    private static final Logger logger = LoggerFactory.getLogger(HylianShield.class);

    private static final long REFRESH_TIME_INTERVAL_MS = 60000L;

    private final String appId;
    private final String appSecret;
    private final String serverURL;

    public HylianShield(String appId,
                        String appSecret,
                        String serverURL) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.serverURL = serverURL.endsWith("/") ? serverURL : serverURL + "/";
    }

    /**
     * 安全登录检测
     *
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @return 检测通过返回true，否则返回false
     */
    public boolean shelter(HttpServletRequest httpRequest,
                           HttpServletResponse httpResponse) throws IOException {
        String path = HTTPUtils.getRequestPath(httpRequest);
        if (path != null && path.equals(Constants.CLIENT_PATH_LOGOUT)) {
            httpResponse.sendRedirect(String.format("%s%s?%s=%s&%s=%s", serverURL,
                    Constants.SERVER_PATH_LOGOUT, Constants.PARAM_APP_ID, appId,
                    Constants.PARAM_APP_SECRET, appSecret));
            SessionManager.invalidate(httpRequest.getSession().getId());
            return false;
        } else if (path != null && path.equals(Constants.CLIENT_PATH_SWEEP)) {
            sweepSession(httpRequest);
            return false;
        }
        String token = SessionUtils.getToken(httpRequest);
        if (StringUtils.isNotEmpty(token)) {
            if (refreshUser(token, httpRequest) && refreshToken(token, httpRequest)) return true;
            logger.warn("Token is expired");
        } else {
            String authorization = httpRequest.getHeader(Constants.HEADER_AUTHORIZATION);
            String sessionId = SessionUtils.getSessionID(httpRequest);
            String cookieSessionId = CookieUtils.getCookie(httpRequest, Constants.COOKIE_SESSION_ID);
            if (StringUtils.isNotEmpty(authorization) && !Objects.equals(sessionId, cookieSessionId)) {
                logger.error("Cookie session id is expired, need refresh");
                ClientErrorException e = new ClientErrorException("Need refresh session", Response.Status.CONFLICT);
                httpRequest.getServletContext().setAttribute(Constants.ATTRIBUTE_EXCEPTION, e);
                throw e;
            }
        }
        SessionUtils.removeResources(httpRequest);
        String code = httpRequest.getParameter(Constants.PARAM_CODE);
        if (StringUtils.isEmpty(code)) {
            logger.info("Apply code for acquiring token");
            String redirectURL = HTTPUtils.getRequestURL(httpRequest);
            String authorization = httpRequest.getHeader(Constants.HEADER_AUTHORIZATION);
            /*
             * 解决微信小程序对303支持不好问题：微信小程序返回302，其他返回303
             */
            if (StringUtils.isNotEmpty(authorization)) {
                httpResponse.sendRedirect(String.format("%s%s?%s=%s&%s=%s&%s=%s", serverURL,
                        Constants.SERVER_PATH_APPLY_CODE, Constants.PARAM_APP_ID, appId,
                        Constants.PARAM_APP_SECRET, appSecret, Constants.PARAM_REDIRECT_URL,
                        URLEncoder.encode(redirectURL, StandardCharsets.UTF_8)));
                return false;
            }
            httpResponse.setStatus(HttpServletResponse.SC_SEE_OTHER);
            httpResponse.setHeader(Constants.HEADER_LOCATION, String.format("%s%s?%s=%s&%s=%s&%s=%s", serverURL,
                    Constants.SERVER_PATH_APPLY_CODE, Constants.PARAM_APP_ID, appId,
                    Constants.PARAM_APP_SECRET, appSecret, Constants.PARAM_REDIRECT_URL,
                    URLEncoder.encode(redirectURL, StandardCharsets.UTF_8)));
            return false;
        }
        String requestURL = HTTPUtils.removeQueries(HTTPUtils.getRequestURL(httpRequest),
                new HashSet<>() {{ add(Constants.PARAM_CODE); }});
        ReentrantLock sessionLock = SessionUtils.getLock(httpRequest);
        try {
            if (sessionLock != null) sessionLock.lock();
            if (SessionUtils.getToken(httpRequest) != null) {
                httpResponse.sendRedirect(requestURL);
                return false;
            }
            token = acquireToken(code, httpRequest);
            if (StringUtils.isEmpty(token)) {
                logger.error("Acquire token failed");
                httpResponse.sendRedirect(requestURL);
                return false;
            }
            logger.info("Acquire token success");
            SessionManager.putTokenSession(httpRequest.getSession());
            SessionUtils.setToken(httpRequest, token);
            httpResponse.sendRedirect(requestURL);
            return false;
        } finally {
            if (sessionLock != null) sessionLock.unlock();
        }
    }

    /**
     * 清理登录session
     *
     * @param httpRequest HTTP请求
     */
    private void sweepSession(HttpServletRequest httpRequest) {
        Map<String, String> queryMap = HTTPUtils.getRequestQueryMap(httpRequest);
        String sessionId = queryMap.getOrDefault(Constants.PARAM_SESSION_ID, null);
        SessionManager.invalidate(sessionId);
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
        HttpRequest request = HttpRequest.buildGetRequest(requestURL, paramMap);
        return HTTPExecutor.executeAndUnwrap(request, String.class);
    }

    /**
     * 根据token向服务端获取用户信息
     *
     * @param token 令牌
     * @return 成功返回用户信息，否则返回null
     */
    private User getUser(String token) {
        String requestURL = String.format("%s%s", serverURL, Constants.SERVER_PATH_GET_USER);
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
    private Tenant getTenant(String token) {
        String requestURL = String.format("%s%s", serverURL, Constants.SERVER_PATH_GET_TENANT);
        Map<String, Object> paramMap = buildTokenRequest(token);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        WebResponse<Tenant> response = HTTPExecutor.execute(httpRequest, Tenant.class);
        if (response == null || !response.status) return null;
        return response.data;
    }

    /**
     * 刷新token
     *
     * @param token 原token
     * @param httpServletRequest HTTP请求
     * @return 刷新成功返回true，否则返回false
     */
    private boolean refreshToken(String token, HttpServletRequest httpServletRequest) {
        Long refreshTime = SessionUtils.getTokenRefreshTime(httpServletRequest);
        long refreshInterval = refreshTime == null ? Long.MAX_VALUE : System.currentTimeMillis() - refreshTime;
        if (refreshInterval <= REFRESH_TIME_INTERVAL_MS) return true;
        ReentrantLock sessionLock = SessionUtils.getLock(httpServletRequest);
        try {
            if (sessionLock != null) sessionLock.lock();
            if (!token.equals(SessionUtils.getToken(httpServletRequest))) return true;
            String requestURL = String.format("%s%s", serverURL, Constants.SERVER_PATH_REFRESH_TOKEN);
            Map<String, Object> body = new HashMap<>();
            body.put(Constants.PARAM_TOKEN, token);
            body.put(Constants.PARAM_APP_ID, appId);
            body.put(Constants.PARAM_APP_SECRET, appSecret);
            HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, body);
            String newToken = HTTPExecutor.executeAndUnwrap(httpRequest, String.class);
            if (StringUtils.isEmpty(newToken)) return false;
            SessionUtils.setToken(httpServletRequest, newToken);
            return true;
        } finally {
            if (sessionLock != null) sessionLock.unlock();
        }
    }

    /**
     * 刷新session用户信息
     *
     * @param token 令牌
     * @param httpRequest HTTP请求
     * @return 成功返回true，否则返回false
     */
    private boolean refreshUser(String token, HttpServletRequest httpRequest) {
        boolean forceRefresh = SessionUtils.isRefreshUser(httpRequest);
        if (!forceRefresh && SessionUtils.getUser(httpRequest) != null) return true;
        User user = getUser(token);
        if (user == null) {
            logger.error("Get user failed for token:{}", token);
            return false;
        }
        SessionUtils.setUser(httpRequest, user);
        SessionUtils.removeRefreshUser(httpRequest);
        return true;
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
        paramMap.put(Constants.PARAM_APP_ID, appId);
        paramMap.put(Constants.PARAM_APP_SECRET, appSecret);
        return paramMap;
    }
}
