package xin.manong.security.keeper.server.controller;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.security.keeper.common.util.CookieUtils;
import xin.manong.security.keeper.common.util.SessionUtils;
import xin.manong.security.keeper.model.*;
import xin.manong.security.keeper.model.view.request.UserSearchRequest;
import xin.manong.security.keeper.server.common.Constants;
import xin.manong.security.keeper.server.request.AcquireTokenRequest;
import xin.manong.security.keeper.server.request.AppRolePermissionsRequest;
import xin.manong.security.keeper.server.request.LoginRequest;
import xin.manong.security.keeper.server.request.RemoveAppLoginRequest;
import xin.manong.security.keeper.server.service.*;
import xin.manong.security.keeper.server.service.request.RolePermissionSearchRequest;
import xin.manong.security.keeper.server.service.request.UserRoleSearchRequest;
import xin.manong.security.keeper.sso.client.core.RefreshTokenRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 安全控制器
 *
 * @author frankcl
 * @date 2023-08-31 15:33:28
 */
@RestController
@Controller
@Path("/api/security")
@RequestMapping("/api/security")
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
    protected RoleService roleService;
    @Resource
    protected UserRoleService userRoleService;
    @Resource
    protected RolePermissionService rolePermissionService;
    @Resource
    protected PermissionService permissionService;
    @Resource
    protected CaptchaService captchaService;

    /**
     * 申请安全code
     * 成功重定向到redirectURL，否则重定向到登录URL
     *
     * @param appId 应用ID
     * @param appSecret 应用秘钥
     * @param redirectURL 重定向URL
     * @param httpRequest HTTP请求对象
     * @param httpResponse HTTP响应对象
     * @return 申请码
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("applyCode")
    @GetMapping("applyCode")
    @EnableWebLogAspect
    public String applyCode(@QueryParam("app_id")  @RequestParam("app_id") String appId,
                            @QueryParam("app_secret") @RequestParam("app_secret") String appSecret,
                            @QueryParam("redirect_url") @RequestParam("redirect_url") String redirectURL,
                            @Context HttpServletRequest httpRequest,
                            @Context HttpServletResponse httpResponse) throws IOException {
        appService.verifyApp(appId, appSecret);
        String ticket = CookieUtils.getCookie(httpRequest, Constants.COOKIE_TICKET);
        verifyTicket(ticket);
        String code = codeService.createCode(ticket);
        boolean hasQuery = !StringUtils.isEmpty(new URL(redirectURL).getQuery());
        httpResponse.sendRedirect(String.format("%s%s%s=%s", redirectURL,
                hasQuery ? "&" : "?", Constants.PARAM_CODE, code));
        return code;
    }

    /**
     * 检测token有效性
     *
     * @param token 令牌
     * @return 有效返回true，否则返回false
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("checkToken")
    @GetMapping("checkToken")
    @EnableWebLogAspect
    public boolean checkToken(@QueryParam("token") @RequestParam("token") String token,
                              @QueryParam("app_id") @RequestParam("app_id") String appId,
                              @QueryParam("app_secret") @RequestParam("app_secret") String appSecret) {
        appService.verifyApp(appId, appSecret);
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
    @Path("acquireToken")
    @GetMapping("acquireToken")
    @EnableWebLogAspect
    public String acquireToken(@BeanParam AcquireTokenRequest request,
                               @Context HttpServletRequest httpRequest) {
        if (request == null) {
            logger.error("acquire token request is empty");
            throw new BadRequestException("获取token请求为空");
        }
        request.check();
        appService.verifyApp(request.appId, request.appSecret);
        String ticket = codeService.getTicket(request.code);
        codeService.removeCode(request.code);
        verifyTicket(ticket);
        Profile profile = jwtService.decodeProfile(ticket);
        String token = tokenService.buildToken(profile, Constants.CACHE_TOKEN_EXPIRED_TIME_MS);
        tokenService.putTokenTicket(token, ticket);
        ticketService.addToken(profile.id, token);
        if (!appLoginService.isLogin(request.appId, request.sessionId)) {
            AppLogin appLogin = new AppLogin().setAppId(request.appId).setUserId(profile.userId).
                    setTicketId(profile.id).setSessionId(request.sessionId);
            if (!appLoginService.add(appLogin)) {
                logger.warn("add app login failed for app[{}] and user[{}]", appLogin.appId, appLogin.userId);
            }
        }
        return token;
    }

    /**
     * 获取用户信息
     *
     * @param token 令牌
     * @param appId 应用ID
     * @param appSecret 应用秘钥
     * @return 成功返回用户信息，否则返回null
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getUser")
    @GetMapping("getUser")
    @EnableWebLogAspect
    public User getUser(@QueryParam("token") @RequestParam("token") String token,
                        @QueryParam("app_id") @RequestParam("app_id") String appId,
                        @QueryParam("app_secret") @RequestParam("app_secret") String appSecret,
                        @Context HttpServletRequest httpRequest) {
        appService.verifyApp(appId, appSecret);
        verifyToken(token);
        Profile profile = jwtService.decodeProfile(token);
        User user = userService.get(profile.userId);
        if (user == null) {
            logger.error("user[{}] is not found", profile.userId);
            throw new NotFoundException(String.format("用户[%s]不存在", profile.userId));
        }
        return user;
    }

    /**
     * 获取租户信息
     *
     * @param token 令牌
     * @param appId 应用ID
     * @param appSecret 应用秘钥
     * @return 成功返回租户信息，否则返回null
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getTenant")
    @GetMapping("getTenant")
    @EnableWebLogAspect
    public Tenant getTenant(@QueryParam("token") @RequestParam("token") String token,
                            @QueryParam("app_id") @RequestParam("app_id") String appId,
                            @QueryParam("app_secret") @RequestParam("app_secret") String appSecret,
                            @Context HttpServletRequest httpRequest) {
        appService.verifyApp(appId, appSecret);
        verifyToken(token);
        Profile profile = jwtService.decodeProfile(token);
        Tenant tenant = tenantService.get(profile.tenantId);
        if (tenant == null) {
            logger.error("tenant[{}] is not found", profile.tenantId);
            throw new NotFoundException(String.format("租户[%s]不存在", profile.tenantId));
        }
        return tenant;
    }

    /**
     * 获取供应商信息
     *
     * @param token 令牌
     * @param appId 应用ID
     * @param appSecret 应用秘钥
     * @return 成功返回供应商信息，否则返回null
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getVendor")
    @GetMapping("getVendor")
    @EnableWebLogAspect
    public Vendor getVendor(@QueryParam("token") @RequestParam("token") String token,
                            @QueryParam("app_id") @RequestParam("app_id") String appId,
                            @QueryParam("app_secret") @RequestParam("app_secret") String appSecret,
                            @Context HttpServletRequest httpRequest) {
        appService.verifyApp(appId, appSecret);
        verifyToken(token);
        Profile profile = jwtService.decodeProfile(token);
        Vendor vendor = vendorService.get(profile.vendorId);
        if (vendor == null) {
            logger.error("vendor[{}] is not found", profile.vendorId);
            throw new NotFoundException(String.format("供应商[%s]不存在", profile.vendorId));
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
    @Path("refreshToken")
    @PostMapping("refreshToken")
    @EnableWebLogAspect
    public String refreshToken(@RequestBody RefreshTokenRequest request,
                               @Context HttpServletRequest httpRequest,
                               @Context HttpServletResponse httpResponse) {
        if (request == null) {
            logger.error("refresh token request is null");
            throw new BadRequestException("刷新token请求为空");
        }
        request.check();
        appService.verifyApp(request.appId, request.appSecret);
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
    @Path("removeAppLogin")
    @PostMapping("removeAppLogin")
    @EnableWebLogAspect
    public boolean removeAppLogin(@RequestBody RemoveAppLoginRequest request) {
        if (request == null) {
            logger.error("remove app login request is null");
            throw new BadRequestException("移除应用登录记录请求为空");
        }
        request.check();
        appService.verifyApp(request.appId, request.appSecret);
        return appLoginService.remove(request.sessionId, request.appId);
    }

    /**
     * 获取应用用户角色列表
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @param appSecret 应用秘钥
     * @return 角色列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getAppUserRoles")
    @GetMapping("getAppUserRoles")
    @EnableWebLogAspect
    public List<Role> getAppUserRoles(@QueryParam("user_id") @RequestParam("user_id") String userId,
                                      @QueryParam("app_id") @RequestParam("app_id") String appId,
                                      @QueryParam("app_secret") @RequestParam("app_secret") String appSecret) {
        if (StringUtils.isEmpty(userId)) {
            logger.error("user id is empty");
            throw new BadRequestException("用户ID为空");
        }
        appService.verifyApp(appId, appSecret);
        UserRoleSearchRequest searchRequest = new UserRoleSearchRequest();
        searchRequest.userId = userId;
        searchRequest.appId = appId;
        searchRequest.current = Constants.DEFAULT_CURRENT;
        searchRequest.size = 100;
        Pager<UserRole> pager = userRoleService.search(searchRequest);
        if (pager == null || pager.records == null) return new ArrayList<>();
        List<String> roleIds = pager.records.stream().map(r -> r.roleId).collect(Collectors.toList());
        return roleService.batchGet(roleIds);
    }

    /**
     * 获取应用角色权限列表
     *
     * @param request 应用角色权限请求
     * @return 权限列表
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getAppRolePermissions")
    @PostMapping("getAppRolePermissions")
    @EnableWebLogAspect
    public List<Permission> getAppRolePermissions(@RequestBody AppRolePermissionsRequest request) {
        if (request == null) {
            logger.error("role permission request is null");
            throw new BadRequestException("角色权限请求为空");
        }
        request.check();
        appService.verifyApp(request.appId, request.appSecret);
        RolePermissionSearchRequest searchRequest = new RolePermissionSearchRequest();
        searchRequest.roleIds = request.roleIds;
        searchRequest.current = Constants.DEFAULT_CURRENT;
        searchRequest.size = request.size;
        Pager<RolePermission> pager = rolePermissionService.search(searchRequest);
        if (pager == null || pager.records == null) return new ArrayList<>();
        List<String> permissionIds = pager.records.stream().map(r -> r.permissionId).
                distinct().collect(Collectors.toList());
        return permissionService.batchGet(permissionIds);
    }

    /**
     * 应用注销
     *
     * @param appId 应用ID
     * @param appSecret 应用秘钥
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("logout")
    @GetMapping("logout")
    @EnableWebLogAspect
    public boolean logout(@QueryParam("app_id") @RequestParam("app_id") String appId,
                          @QueryParam("app_secret") @RequestParam("app_secret") String appSecret,
                          @Context HttpServletRequest httpRequest,
                          @Context HttpServletResponse httpResponse) throws IOException {
        appService.verifyApp(appId, appSecret);
        CookieUtils.removeCookie(Constants.COOKIE_TOKEN, "/", httpResponse);
        String ticket = CookieUtils.getCookie(httpRequest, Constants.COOKIE_TICKET);
        if (StringUtils.isEmpty(ticket)) {
            logger.error("ticket is not found from cookies");
            throw new RuntimeException("尚未登录");
        }
        removeTicketResources(ticket);
        CookieUtils.removeCookie(Constants.COOKIE_TICKET, "/", httpResponse);
        Profile profile = jwtService.decodeProfile(ticket);
        if (profile != null) appLoginService.remove(profile.id);
        logger.info("logout success for user[{}]", profile != null ? profile.userId : "null");
        return true;
    }

    /**
     * 应用登录
     *
     * @param request 登录请求
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("passwordLogin")
    @PostMapping("passwordLogin")
    @EnableWebLogAspect
    public boolean passwordLogin(@RequestBody LoginRequest request,
                                 @Context HttpServletRequest httpRequest,
                                 @Context HttpServletResponse httpResponse) {
        if (isLogin(httpRequest)) {
            logger.info("previously logged in");
            return true;
        }
        request.check();
        String sessionId = SessionUtils.getSessionID(httpRequest);
        String captcha = captchaService.get(sessionId);
        if (captcha == null || !captcha.equalsIgnoreCase(request.captcha)) {
            logger.error("captcha is incorrect");
            throw new BadRequestException("验证码不正确");
        }
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.userName = request.username.trim();
        searchRequest.current = 1;
        searchRequest.size = 1;
        Pager<User> pager = userService.search(searchRequest);
        if (pager == null || pager.total < 1 || pager.records.isEmpty()) {
            logger.error("user is not found for username[{}]", request.username);
            throw new NotFoundException(String.format("用户[%s]不存在", request.username));
        }
        User user = pager.records.get(0);
        if (!user.password.equals(DigestUtils.md5Hex(request.password.trim()))) {
            logger.error("username and password are not matched");
            throw new RuntimeException("用户名和密码不匹配");
        }
        Profile profile = new Profile();
        profile.setId(RandomID.build()).setUserId(user.id).setTenantId(user.tenantId).setVendorId(user.vendorId);
        String ticket = ticketService.buildTicket(profile, Constants.COOKIE_TICKET_EXPIRED_TIME_MS);
        ticketService.putTicket(profile.id, ticket);
        CookieUtils.setCookie(Constants.COOKIE_TICKET, ticket, "/", true, httpRequest, httpResponse);
        CookieUtils.setCookie(Constants.COOKIE_TOKEN, RandomID.build(), "/", false, httpRequest, httpResponse);
        logger.info("login success for user[{}]", profile.userId);
        return true;
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
        verifyTicket(ticket);
        return true;
    }

    /**
     * 验证token
     * 1. 检测token有效性
     * 2. 检测token对应ticket
     *
     * @param token 令牌
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
        verifyTicket(ticket);
    }

    /**
     * 验证ticket
     *
     * @param ticket 票据
     */
    private void verifyTicket(String ticket) {
        if (StringUtils.isEmpty(ticket)) {
            logger.warn("ticket is empty");
            throw new NotAuthorizedException("ticket为空");
        }
        if (!ticketService.verifyTicket(ticket)) {
            logger.error("verify ticket failed");
            removeTicketResources(ticket);
            throw new NotAuthorizedException("验证ticket失败");
        }
        Profile profile = jwtService.decodeProfile(ticket);
        if (profile == null) {
            logger.error("decode profile failed from ticket");
            removeTicketResources(ticket);
            throw new NotAuthorizedException("非法ticket");
        }
        String cachedTicket = ticketService.getTicket(profile.id);
        if (StringUtils.isEmpty(cachedTicket) || !ticket.equals(cachedTicket)) {
            logger.error("cached ticket and provided ticket are not consistent");
            removeTicketResources(ticket);
            throw new NotAuthorizedException("非法ticket");
        }
    }

    /**
     * 移除ticket相关资源
     * 1. 移除ticket相关token
     * 2. 移除ticket
     *
     * @param ticket 票据
     */
    private void removeTicketResources(String ticket) {
        Profile profile = jwtService.decodeProfile(ticket);
        if (profile == null) return;
        Set<String> tokenIds = ticketService.getTicketTokens(profile.id);
        for (String tokenId : tokenIds) tokenService.removeTokenTicketWithId(tokenId);
        ticketService.removeTicketTokens(profile.id);
        ticketService.removeTicket(profile.id);
    }

    /**
     * 移除token相关资源
     *
     * @param token 令牌
     */
    private void removeTokenResources(String token) {
        tokenService.removeTokenTicket(token);
        Profile profile = jwtService.decodeProfile(token);
        if (profile == null) return;
        ticketService.removeToken(profile.id, token);
    }
}
