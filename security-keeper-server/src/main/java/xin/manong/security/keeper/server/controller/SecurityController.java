package xin.manong.security.keeper.server.controller;

import okhttp3.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.security.keeper.common.util.CookieUtils;
import xin.manong.security.keeper.common.util.HTTPUtils;
import xin.manong.security.keeper.model.*;
import xin.manong.security.keeper.server.common.Constants;
import xin.manong.security.keeper.model.request.RefreshTokenRequest;
import xin.manong.security.keeper.server.request.AcquireTokenRequest;
import xin.manong.security.keeper.server.request.RemoveAppLoginRequest;
import xin.manong.security.keeper.server.service.*;
import xin.manong.security.keeper.server.service.request.AppLoginSearchRequest;
import xin.manong.security.keeper.server.service.request.UserSearchRequest;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.util.RandomID;
import xin.manong.weapon.spring.web.ws.aspect.EnableWebLogAspect;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 安全控制器
 *
 * @author frankcl
 * @date 2023-08-31 15:33:28
 */
@RestController
@Controller
@Path("/security")
@RequestMapping("/security")
public class SecurityController {

    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);

    @Resource
    protected JWTService jwtService;
    @Resource
    protected CodeService codeService;
    @Resource
    protected TokenService tokenService;
    @Resource
    protected TicketService ticketService;
    @Resource
    protected AppService appService;
    @Resource
    protected UserService userService;
    @Resource
    protected TenantService tenantService;
    @Resource
    protected VendorService vendorService;
    @Resource
    protected AppLoginService appLoginService;
    @Resource
    protected HttpClient httpClient;

    /**
     * 申请安全code
     * 成功重定向到redirectURL，否则重定向到登录URL
     *
     * @param appId 应用ID
     * @param appSecret 应用秘钥
     * @param redirectURL 重定向URL
     * @param httpRequest HTTP请求对象
     * @param httpResponse HTTP响应对象
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("auth/applyCode")
    @GetMapping("auth/applyCode")
    @EnableWebLogAspect
    public void applyCode(@QueryParam("app_id")  String appId,
                          @QueryParam("app_secret") String appSecret,
                          @QueryParam("redirect_url") String redirectURL,
                          @Context HttpServletRequest httpRequest,
                          @Context HttpServletResponse httpResponse) throws Exception {
        if (StringUtils.isEmpty(redirectURL)) {
            logger.error("redirect url is empty");
            throw new BadRequestException("重定向URL为空");
        }
        verifyApp(appId, appSecret);
        String ticket = CookieUtils.getCookie(httpRequest, Constants.COOKIE_TICKET);
        if (StringUtils.isEmpty(ticket) || !verifyTicket(ticket)) {
            if (ticket != null) CookieUtils.removeCookie(Constants.COOKIE_TICKET, "/", httpResponse);
            httpResponse.sendRedirect(String.format("%s%s?%s=%s",
                    HTTPUtils.getRequestRootURL(httpRequest), Constants.PATH_LOGIN, Constants.PARAM_REDIRECT_URL,
                    URLEncoder.encode(redirectURL, Constants.CHARSET_UTF8)));
            return;
        }
        String code = codeService.createCode(ticket);
        boolean hasQuery = !StringUtils.isEmpty(new URL(redirectURL).getQuery());
        httpResponse.sendRedirect(String.format("%s%s%s=%s", redirectURL,
                hasQuery ? "&" : "?", Constants.PARAM_CODE, code));
    }

    /**
     * 检测token有效性
     *
     * @param token
     * @return 有效返回true，否则返回false
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("auth/checkToken")
    @GetMapping("auth/checkToken")
    @EnableWebLogAspect
    public boolean checkToken(@QueryParam("token") String token,
                              @QueryParam("app_id")  String appId,
                              @QueryParam("app_secret") String appSecret) {
        verifyApp(appId, appSecret);
        verifyToken(token);
        return true;
    }

    /**
     * 获取token
     *
     * @param request 请求
     * @return 成功返回token
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("auth/acquireToken")
    @GetMapping("auth/acquireToken")
    @EnableWebLogAspect
    public String acquireToken(@BeanParam AcquireTokenRequest request,
                               @Context HttpServletRequest httpRequest) {
        if (request == null) {
            logger.error("acquire token request is empty");
            throw new BadRequestException("获取token请求为空");
        }
        request.check();
        verifyApp(request.appId, request.appSecret);
        String ticket = codeService.getTicket(request.code);
        if (!verifyTicket(ticket)) {
            logger.error("ticket is not found for code[{}] or verify ticket failed", request.code);
            throw new RuntimeException(String.format("未找到code[%s]对应ticket或ticket验证失败", request.code));
        }
        codeService.removeCode(request.code);
        Profile profile = jwtService.decodeProfile(ticket);
        String token = tokenService.buildToken(profile, Constants.CACHE_TOKEN_EXPIRED_TIME_MS);
        if (StringUtils.isEmpty(token)) {
            logger.error("build token failed");
            throw new RuntimeException("构建token失败");
        }
        tokenService.putTokenTicket(token, ticket);
        ticketService.addToken(profile.id, token);
        AppLoginSearchRequest searchRequest = new AppLoginSearchRequest();
        searchRequest.userId = profile.userId;
        searchRequest.appId = request.appId;
        searchRequest.sessionId = request.sessionId;
        if (!appLoginService.isLoginApp(searchRequest)) {
            AppLogin appLogin = new AppLogin().setAppId(request.appId).setUserId(profile.userId).
                    setTicketId(profile.id).setSessionId(request.sessionId).setLogoutURL(request.logoutURL);
            if (!appLoginService.add(appLogin)) {
                logger.warn("add app login failed for app[{}] and user[{}]", appLogin.appId, appLogin.userId);
            }
        }
        return token;
    }

    /**
     * 获取用户信息
     *
     * @param token 安全token
     * @param appId 应用ID
     * @param appSecret 应用秘钥
     * @return 成功返回用户信息，否则返回null
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("resource/getUser")
    @GetMapping("resource/getUser")
    @EnableWebLogAspect
    public User getUser(@QueryParam("token")  String token,
                        @QueryParam("app_id")  String appId,
                        @QueryParam("app_secret") String appSecret,
                        @Context HttpServletRequest httpRequest) {
        verifyApp(appId, appSecret);
        verifyToken(token);
        Profile profile = jwtService.decodeProfile(token);
        User user = userService.get(profile.userId);
        if (user == null) {
            logger.error("user[{}] is not found", profile.userId);
            throw new RuntimeException(String.format("用户[%s]不存在", profile.userId));
        }
        return user;
    }

    /**
     * 获取租户信息
     *
     * @param token 安全token
     * @param appId 应用ID
     * @param appSecret 应用秘钥
     * @return 成功返回租户信息，否则返回null
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("resource/getTenant")
    @GetMapping("resource/getTenant")
    @EnableWebLogAspect
    public Tenant getTenant(@QueryParam("token")  String token,
                            @QueryParam("app_id")  String appId,
                            @QueryParam("app_secret") String appSecret,
                            @Context HttpServletRequest httpRequest) {
        verifyApp(appId, appSecret);
        verifyToken(token);
        Profile profile = jwtService.decodeProfile(token);
        Tenant tenant = tenantService.get(profile.tenantId);
        if (tenant == null) {
            logger.error("tenant[{}] is not found", profile.tenantId);
            throw new RuntimeException(String.format("租户[%s]不存在", profile.tenantId));
        }
        return tenant;
    }

    /**
     * 获取供应商信息
     *
     * @param token 安全token
     * @param appId 应用ID
     * @param appSecret 应用秘钥
     * @return 成功返回供应商信息，否则返回null
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("resource/getVendor")
    @GetMapping("resource/getVendor")
    @EnableWebLogAspect
    public Vendor getVendor(@QueryParam("token")  String token,
                            @QueryParam("app_id")  String appId,
                            @QueryParam("app_secret") String appSecret,
                            @Context HttpServletRequest httpRequest) {
        verifyApp(appId, appSecret);
        verifyToken(token);
        Profile profile = jwtService.decodeProfile(token);
        Vendor vendor = vendorService.get(profile.vendorId);
        if (vendor == null) {
            logger.error("vendor[{}] is not found", profile.vendorId);
            throw new RuntimeException(String.format("供应商[%s]不存在", profile.vendorId));
        }
        return vendor;
    }

    /**
     * 刷新token
     *
     * @param request 刷新请求
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @return 成功返回新token，否则抛出异常
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("auth/refreshToken")
    @PostMapping("auth/refreshToken")
    @EnableWebLogAspect
    public String refreshToken(@RequestBody RefreshTokenRequest request,
                               @Context HttpServletRequest httpRequest,
                               @Context HttpServletResponse httpResponse) {
        if (request == null) {
            logger.error("refresh token request is null");
            throw new BadRequestException("刷新token请求为空");
        }
        request.check();
        verifyApp(request.appId, request.appSecret);
        verifyToken(request.token);
        String ticket = tokenService.getTicket(request.token);
        Profile profile = jwtService.decodeProfile(ticket);
        tokenService.removeTokenTicket(request.token);
        ticketService.removeToken(profile.id, request.token);
        String newToken = tokenService.buildToken(profile, Constants.CACHE_TOKEN_EXPIRED_TIME_MS);
        tokenService.putTokenTicket(newToken, ticket);
        ticketService.addToken(profile.id, newToken);
        ticketService.putTicket(profile.id, ticket);
        return newToken;
    }

    /**
     * 移除应用登录记录
     *
     * @param request 移除请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("auth/removeAppLogin")
    @PostMapping("auth/removeAppLogin")
    @EnableWebLogAspect
    public boolean removeAppLogin(@RequestBody RemoveAppLoginRequest request) {
        if (request == null) {
            logger.error("remove app login request is null");
            throw new BadRequestException("移除应用登录记录请求为空");
        }
        request.check();
        verifyApp(request.appId, request.appSecret);
        return appLoginService.removeAppLogin(request.sessionId, request.appId);
    }

    /**
     * 注销
     *
     * @param appId 应用ID
     * @param appSecret 应用秘钥
     * @param redirectURL 重定向URL
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     */
    @GET
    @Path("sso/logout")
    @GetMapping("sso/logout")
    @EnableWebLogAspect
    public void logout(@QueryParam("app_id")  String appId,
                       @QueryParam("app_secret") String appSecret,
                       @QueryParam("redirect_url") String redirectURL,
                       @Context HttpServletRequest httpRequest,
                       @Context HttpServletResponse httpResponse) throws IOException {
        if (StringUtils.isEmpty(redirectURL)) {
            logger.error("redirect url is empty");
            throw new BadRequestException("重定向URL为空");
        }
        verifyApp(appId, appSecret);
        String ticket = CookieUtils.getCookie(httpRequest, Constants.COOKIE_TICKET);
        if (StringUtils.isEmpty(ticket)) {
            logger.error("ticket is not found from cookies");
            httpResponse.sendRedirect(redirectURL);
            return;
        }
        removeTicketResources(ticket);
        CookieUtils.removeCookie(Constants.COOKIE_TICKET, "/", httpResponse);
        Profile profile = jwtService.decodeProfile(ticket);
        if (profile != null) removeAppLogins(profile.id);
        httpResponse.sendRedirect(redirectURL);
    }

    /**
     * 登录
     *
     * @param userName 用户名
     * @param password 密码
     * @param redirectURL 重定向URL
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @throws IOException
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("sso/login")
    @PostMapping("sso/login")
    @EnableWebLogAspect
    public void login(@FormParam("user_name") String userName,
                      @FormParam("password") String password,
                      @FormParam("redirect_url") String redirectURL,
                      @Context HttpServletRequest httpRequest,
                      @Context HttpServletResponse httpResponse) throws IOException {
        if (isLogin(httpRequest)) {
            logger.info("previously logged in");
            httpResponse.sendRedirect(redirectURL);
            return;
        }
        if (StringUtils.isEmpty(userName)) {
            logger.error("username is empty");
            throw new BadRequestException("用户名为空");
        }
        if (StringUtils.isEmpty(password)) {
            logger.error("password is empty");
            throw new BadRequestException("密码为空");
        }
        if (StringUtils.isEmpty(redirectURL)) {
            redirectURL = String.format("%s%s", HTTPUtils.getRequestRootURL(httpRequest), Constants.PATH_HOME);
        }
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.userName = userName;
        searchRequest.current = 1;
        searchRequest.size = 1;
        Pager<User> pager = userService.search(searchRequest);
        if (pager == null || pager.total < 1 || pager.records.isEmpty()) {
            logger.error("user is not found for username[{}]", userName);
            throw new NotFoundException(String.format("用户[%s]不存在", userName));
        }
        User user = pager.records.get(0);
        if (!user.password.equals(DigestUtils.md5Hex(password))) {
            logger.error("username and password are not matched");
            throw new RuntimeException("用户名和密码不匹配");
        }
        Profile profile = new Profile();
        profile.setId(RandomID.build()).setUserId(user.id).setTenantId(user.tenantId).setVendorId(user.vendorId);
        String ticket = ticketService.buildTicket(profile, Constants.COOKIE_TICKET_EXPIRED_TIME_MS);
        ticketService.putTicket(profile.id, ticket);
        CookieUtils.setCookie(Constants.COOKIE_TICKET, ticket, "/", httpRequest, httpResponse);
        httpResponse.sendRedirect(redirectURL);
    }

    /**
     * 判断是否已经登录
     *
     * @param httpRequest HTTP请求
     * @return 已经登录返回true，否则返回false
     */
    private boolean isLogin(HttpServletRequest httpRequest) {
        String ticket = CookieUtils.getCookie(httpRequest, Constants.COOKIE_TICKET);
        if (StringUtils.isEmpty(ticket)) return false;
        if (!verifyTicket(ticket)) {
            Profile profile = jwtService.decodeProfile(ticket);
            if (profile != null) removeAppLogins(profile.id);
            return false;
        }
        return true;
    }

    /**
     * 注销应用系统并删除应用登录信息
     *
     * @param ticketId ticket ID
     */
    private void removeAppLogins(String ticketId) {
        List<AppLogin> appLogins = appLoginService.getAppLogins(ticketId);
        if (appLogins == null || appLogins.isEmpty()) return;
        for (AppLogin appLogin : appLogins) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put(Constants.PARAM_SESSION_ID, appLogin.sessionId);
            HttpRequest request = HttpRequest.buildGetRequest(appLogin.logoutURL, paramMap);
            Response response = httpClient.execute(request);
            if (response == null || !response.isSuccessful() || response.code() != 200) {
                logger.warn("execute app[{}] logout failed for user[{}] and session[{}]",
                        appLogin.appId, appLogin.userId, appLogin.sessionId);
            } else {
                logger.warn("execute app[{}] logout success for user[{}] and session[{}]",
                        appLogin.appId, appLogin.userId, appLogin.sessionId);
            }
            if (response != null) response.close();
        }
        appLoginService.removeAppLogins(ticketId);
    }

    /**
     * 验证应用信息
     * 验证失败抛出异常
     *
     * @param appId 应用ID
     * @param appSecret 应用秘钥
     */
    private void verifyApp(String appId, String appSecret) {
        if (StringUtils.isEmpty(appId)) {
            logger.error("app id is empty");
            throw new BadRequestException("应用ID为空");
        }
        if (StringUtils.isEmpty(appSecret)) {
            logger.error("app secret is empty");
            throw new BadRequestException("应用秘钥为空");
        }
        App app = appService.get(appId);
        if (app == null) {
            logger.error("app[{}] is not found", appId);
            throw new RuntimeException(String.format("应用[%s]不存在", appId));
        }
        if (!app.secret.equals(appSecret)) {
            logger.error("app id and secret are not matched");
            throw new RuntimeException("应用ID和秘钥不匹配");
        }
    }

    /**
     * 验证token
     * 1. 检测token有效性
     * 2. 检测token对应ticket
     *
     * @param token
     */
    private void verifyToken(String token) {
        if (StringUtils.isEmpty(token)) {
            logger.error("token is empty");
            throw new BadRequestException("token为空");
        }
        if (!tokenService.verifyToken(token)) {
            logger.error("verify token failed");
            removeTokenResources(token);
            throw new RuntimeException("验证token失败");
        }
        String ticket = tokenService.getTicket(token);
        if (!verifyTicket(ticket)) {
            logger.error("verify ticket failed for token[{}]", DigestUtils.md5Hex(token));
            removeTokenResources(token);
            throw new RuntimeException("未找到ticket或验证ticket失败");
        }
    }

    /**
     * 验证ticket
     *
     * @param ticket
     * @return 成功返回true，否则返回false
     */
    private boolean verifyTicket(String ticket) {
        if (StringUtils.isEmpty(ticket)) {
            logger.warn("ticket is empty");
            return false;
        }
        if (!ticketService.verifyTicket(ticket)) {
            logger.error("verify ticket failed");
            removeTicketResources(ticket);
            return false;
        }
        Profile profile = jwtService.decodeProfile(ticket);
        if (profile == null) {
            logger.error("decode profile failed from ticket");
            removeTicketResources(ticket);
            return false;
        }
        String cachedTicket = ticketService.getTicket(profile.id);
        if (StringUtils.isEmpty(cachedTicket) || !ticket.equals(cachedTicket)) {
            logger.error("cached ticket and provided ticket are not consistent");
            removeTicketResources(ticket);
            return false;
        }
        return true;
    }

    /**
     * 移除ticket相关资源
     * 1. 移除ticket相关token资源
     * 2. 资源ticket资源
     *
     * @param ticket
     */
    private void removeTicketResources(String ticket) {
        Profile profile = jwtService.decodeProfile(ticket);
        if (profile == null) return;
        Set<String> tokenIds = ticketService.getTicketTokens(profile.id);
        for (String tokenId : tokenIds) tokenService.removeTokenTicketById(tokenId);
        ticketService.removeTicketTokens(profile.id);
        ticketService.removeTicket(profile.id);
    }

    /**
     * 移除token相关资源
     *
     * @param token
     */
    private void removeTokenResources(String token) {
        tokenService.removeTokenTicket(token);
        Profile profile = jwtService.decodeProfile(token);
        if (profile == null) return;
        ticketService.removeToken(profile.id, token);
    }
}
