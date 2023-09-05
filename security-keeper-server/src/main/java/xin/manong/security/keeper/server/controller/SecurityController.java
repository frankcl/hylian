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
import xin.manong.security.keeper.server.request.LoginRequest;
import xin.manong.security.keeper.server.service.*;
import xin.manong.security.keeper.server.service.request.UserSearchRequest;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.util.RandomID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * 登录认证控制器
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

    private static final String CHARSET_UTF8 = "UTF-8";
    private static final String COOKIE_TICKET = "TICKET";

    private static final String PATH_LOGIN = "/security/sso/login";

    private static final String PARAM_CODE = "code";
    private static final String PARAM_REDIRECT_URL = "redirect_url";

    private static final Long TOKEN_EXPIRED_TIME = 300000L;
    private static final Long TICKET_EXPIRED_TIME = 86400000L;

    @Resource
    protected JWTService jwtService;
    @Resource
    protected CodeService codeService;
    @Resource
    protected AppService appService;
    @Resource
    protected UserService userService;
    @Resource
    protected TenantService tenantService;
    @Resource
    protected VendorService vendorService;
    @Resource
    protected LoginAppService loginAppService;
    @Resource
    protected HttpClient httpClient;

    /**
     * 申请安全code
     *
     * @param appId 应用ID
     * @param redirectURL 重定向URL
     * @param httpRequest HTTP请求对象
     * @param httpResponse HTTP响应对象
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("auth/applyCode")
    @GetMapping("auth/applyCode")
    public void applyCode(@QueryParam("app_id")  String appId,
                          @QueryParam("app_secret")  String appSecret,
                          @QueryParam("redirect_url") String redirectURL,
                          @Context HttpServletRequest httpRequest,
                          @Context HttpServletResponse httpResponse) throws Exception {
        if (StringUtils.isEmpty(redirectURL)) {
            logger.error("redirect url is empty");
            throw new BadRequestException("重定向URL为空");
        }
        verifyApp(appId, appSecret);
        String ticket = CookieUtils.getCookie(httpRequest, COOKIE_TICKET);
        if (StringUtils.isEmpty(ticket) || !jwtService.verifyTicket(ticket)) {
            if (ticket != null) CookieUtils.removeCookie(COOKIE_TICKET, "/", httpResponse);
            httpResponse.sendRedirect(String.format("%s?%s=%s", PATH_LOGIN, PARAM_REDIRECT_URL,
                    URLEncoder.encode(redirectURL, CHARSET_UTF8)));
        }
        String code = codeService.createCode(ticket);
        boolean hasQuery = !StringUtils.isEmpty(new URL(redirectURL).getQuery());
        httpResponse.sendRedirect(String.format("%s%s%s=%s", redirectURL, hasQuery ? "&" : "?", PARAM_CODE, code));
    }

    /**
     * 根据安全code获取token
     *
     * @param code 安全code
     * @param appId 应用ID
     * @param appSecret 应用秘钥
     * @return 成功返回token，否则抛出异常
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("auth/getToken")
    @GetMapping("auth/getToken")
    public String getToken(@QueryParam("code")  String code,
                           @QueryParam("app_id")  String appId,
                           @QueryParam("app_secret") String appSecret,
                           @Context HttpServletRequest httpRequest) {
        if (StringUtils.isEmpty(code)) {
            logger.error("code is empty");
            throw new BadRequestException("安全code为空");
        }
        verifyApp(appId, appSecret);
        String ticket = codeService.getTicket(code);
        if (StringUtils.isEmpty(ticket)) {
            logger.error("ticket is not found for code[{}]", code);
            throw new RuntimeException(String.format("code[%s]对应ticket未找到", code));
        }
        codeService.removeCode(code);
        String token = jwtService.buildTokenWithTicket(ticket, TOKEN_EXPIRED_TIME);
        if (StringUtils.isEmpty(token)) {
            logger.error("build token failed");
            throw new RuntimeException("构建token失败");
        }
        Profile profile = jwtService.decodeProfile(token);
        if (!loginAppService.isLoginApp(profile.id, appId)) {
            LoginApp loginApp = new LoginApp(profile.id, profile.userId, appId,
                    HTTPUtils.getRequestBaseURL(httpRequest));
            if (!loginAppService.register(loginApp)) {
                logger.warn("register login app failed for app[{}] and user[{}]", loginApp.appId, loginApp.userId);
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
    public User getUser(@QueryParam("token")  String token,
                        @QueryParam("app_id")  String appId,
                        @QueryParam("app_secret") String appSecret,
                        @Context HttpServletRequest httpRequest) {
        verifyApp(appId, appSecret);
        Profile profile = verifyAndDecodeProfile(token, httpRequest);
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
    public Tenant getTenant(@QueryParam("token")  String token,
                            @QueryParam("app_id")  String appId,
                            @QueryParam("app_secret") String appSecret,
                            @Context HttpServletRequest httpRequest) {
        verifyApp(appId, appSecret);
        Profile profile = verifyAndDecodeProfile(token, httpRequest);
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
    public Vendor getVendor(@QueryParam("token")  String token,
                            @QueryParam("app_id")  String appId,
                            @QueryParam("app_secret") String appSecret,
                            @Context HttpServletRequest httpRequest) {
        verifyApp(appId, appSecret);
        Profile profile = verifyAndDecodeProfile(token, httpRequest);
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
    public String refreshToken(@RequestBody RefreshTokenRequest request,
                               @Context HttpServletRequest httpRequest,
                               @Context HttpServletResponse httpResponse) {
        if (request == null) {
            logger.error("refresh token request is null");
            throw new BadRequestException("刷新token请求为空");
        }
        request.check();
        verifyApp(request.appId, request.appSecret);
        Profile profile = verifyAndDecodeProfile(request.token, httpRequest);
        String newTicket = jwtService.buildTicket(profile, Constants.ALGORITHM_HS256, TICKET_EXPIRED_TIME);
        String newToken = jwtService.buildTokenWithTicket(newTicket, TOKEN_EXPIRED_TIME);
        CookieUtils.setCookie(COOKIE_TICKET, newTicket, "/", httpRequest, httpResponse);
        return newToken;
    }

    /**
     * 注销登录
     *
     * @param appId 应用ID
     * @param appSecret 应用秘钥
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("sso/logout")
    @GetMapping("sso/logout")
    public void logout(@QueryParam("app_id")  String appId,
                       @QueryParam("app_secret") String appSecret,
                       @Context HttpServletRequest httpRequest,
                       @Context HttpServletResponse httpResponse) {
        verifyApp(appId, appSecret);
        String ticket = CookieUtils.getCookie(httpRequest, COOKIE_TICKET);
        if (StringUtils.isEmpty(ticket) || !jwtService.verifyTicket(ticket)) {
            logger.error("ticket is empty or invalid");
            return;
        }
        Profile profile = jwtService.decodeProfile(ticket);
        if (profile == null) {
            logger.error("decode profile failed from ticket");
            return;
        }
        List<LoginApp> loginApps = loginAppService.getLoginApps(profile.id);
        if (loginApps != null && !loginApps.isEmpty()) {
            for (LoginApp loginApp : loginApps) {
                String logoutURL = String.format("%s/sso/logout", loginApp.baseURL);
                HttpRequest request = HttpRequest.buildGetRequest(logoutURL, null);
                Response response = httpClient.execute(request);
                if (response == null || !response.isSuccessful() || response.code() != 200) {
                    logger.warn("logout failed for url[{}], app[{}] and user[{}]",
                            logoutURL, loginApp.appId, loginApp.userId);
                } else logger.info("logout success for app[{}] and user[{}]", loginApp.appId, loginApp.userId);
                if (response != null) response.close();
            }
        }
        loginAppService.removeLoginApps(profile.id);
        CookieUtils.removeCookie(COOKIE_TICKET, "/", httpResponse);
    }

    /**
     * 登录
     *
     * @param request 登录请求
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @throws IOException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("sso/login")
    @PostMapping("sso/login")
    public void login(@RequestBody LoginRequest request,
                      @Context HttpServletRequest httpRequest,
                      @Context HttpServletResponse httpResponse) throws IOException {
        if (request == null) {
            logger.error("login request is null");
            throw new BadRequestException("登录请求为空");
        }
        if (StringUtils.isEmpty(request.redirectURL)) {
            request.redirectURL = HTTPUtils.getRequestURL(httpRequest);
        }
        request.check();
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.userName = request.userName;
        searchRequest.current = 1;
        searchRequest.size = 1;
        Pager<User> pager = userService.search(searchRequest);
        if (pager == null || pager.total < 1 || pager.records.isEmpty()) {
            logger.error("user is not found for username[{}]", request.userName);
            throw new NotFoundException(String.format("用户[%s]不存在", request.userName));
        }
        User user = pager.records.get(0);
        if (!user.password.equals(DigestUtils.md5Hex(request.password))) {
            logger.error("username and password are not matched");
            throw new RuntimeException("用户名和密码不匹配");
        }
        Profile profile = new Profile(RandomID.build(), user.id, user.tenantId, user.vendorId);
        String ticket = jwtService.buildTicket(profile, Constants.ALGORITHM_HS256, TICKET_EXPIRED_TIME);
        if (StringUtils.isEmpty(ticket)) {
            logger.error("build ticket failed");
            throw new RuntimeException("构建ticket失败");
        }
        CookieUtils.setCookie(COOKIE_TICKET, ticket, "/", httpRequest, httpResponse);
        httpResponse.sendRedirect(request.redirectURL);
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
     * 验证token及解码profile
     *
     * @param token
     * @param httpRequest HTTP请求
     * @return 成功返回profile，否则抛出异常
     */
    private Profile verifyAndDecodeProfile(String token, HttpServletRequest httpRequest) {
        if (StringUtils.isEmpty(token)) {
            logger.error("token is empty");
            throw new BadRequestException("token为空");
        }
        if (!jwtService.verifyToken(token)) {
            logger.error("verify token failed");
            throw new RuntimeException("验证token失败");
        }
        Profile profile = jwtService.decodeProfile(token);
        if (profile == null) {
            logger.error("decode profile failed from token");
            throw new RuntimeException("解码profile失败");
        }
        String ticket = CookieUtils.getCookie(httpRequest, COOKIE_TICKET);
        Profile ticketProfile = jwtService.decodeProfile(ticket);
        if (ticketProfile == null || !ticketProfile.id.equals(profile.id)) {
            logger.error("token and ticket are not consistent");
            throw new RuntimeException("token和ticket不一致");
        }
        return profile;
    }
}
