package xin.manong.hylian.server.controller;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.hylian.model.*;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.server.model.UserProfile;
import xin.manong.hylian.server.util.CookieUtils;
import xin.manong.hylian.client.util.SessionUtils;
import xin.manong.hylian.server.service.request.UserSearchRequest;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.controller.request.AcquireTokenRequest;
import xin.manong.hylian.server.controller.request.AppRolePermissionsRequest;
import xin.manong.hylian.server.controller.request.LoginRequest;
import xin.manong.hylian.server.controller.request.RefreshTokenRequest;
import xin.manong.hylian.server.controller.request.RemoveActivityRequest;
import xin.manong.hylian.server.service.*;
import xin.manong.hylian.server.service.request.RolePermissionSearchRequest;
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
import java.util.function.Function;
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
    protected AppUserService appUserService;
    @Resource
    protected TenantService tenantService;
    @Resource
    protected ActivityService activityService;
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
     * 成功重定向到redirectURL，否则抛出异常NotAuthorizedException
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
        boolean hasQuery = StringUtils.isNotEmpty(new URL(redirectURL).getQuery());
        httpResponse.sendRedirect(String.format("%s%s%s=%s", redirectURL,
                hasQuery ? "&" : "?", Constants.PARAM_CODE, code));
        return code;
    }

    /**
     * 获取token
     * 认证失败抛出异常NotAuthorizedException
     *
     * @param request 请求
     * @return 成功返回token
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("acquireToken")
    @GetMapping("acquireToken")
    @EnableWebLogAspect
    public String acquireToken(@BeanParam AcquireTokenRequest request) {
        if (request == null) throw new BadRequestException("获取token请求为空");
        request.check();
        appService.verifyApp(request.appId, request.appSecret);
        String ticket = codeService.getTicket(request.code);
        codeService.removeCode(request.code);
        verifyTicket(ticket);
        UserProfile userProfile = jwtService.decodeProfile(ticket);
        String token = tokenService.buildToken(userProfile, Constants.CACHE_TOKEN_EXPIRED_TIME_MS);
        tokenService.putToken(token, ticket);
        ticketService.addToken(userProfile.id, token);
        Activity prevActivity = activityService.get(request.appId, userProfile.id);
        Activity activity = new Activity();
        activity.userId = userProfile.userId;
        activity.ticketId = userProfile.id;
        activity.sessionId = request.sessionId;
        activity.appId = request.appId;
        if (prevActivity != null) activity.id = prevActivity.id;
        Function<Activity, Boolean> function = prevActivity == null ?
                record -> activityService.add(record) : record -> activityService.update(record);
        if (!function.apply(activity)) {
            String operation = prevActivity == null ? "add" : "update";
            logger.warn("{} activity failed for app[{}] and user[{}]", operation , request.appId, userProfile.userId);
        }
        return token;
    }

    /**
     * 获取用户信息
     * 认证失败抛出异常NotAuthorizedException
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
                        @QueryParam("app_secret") @RequestParam("app_secret") String appSecret) {
        appService.verifyApp(appId, appSecret);
        if (!verifyToken(token)) return null;
        UserProfile userProfile = jwtService.decodeProfile(token);
        User user = userService.get(userProfile.userId);
        if (user == null) {
            logger.error("user[{}] is not found", userProfile.userId);
            throw new NotAuthorizedException("用户不存在");
        }
        if (appUserService.getAppUser(appId, user.id) != null) user.superAdmin = true;
        return user;
    }

    /**
     * 获取租户信息
     * 认证失败抛出异常NotAuthorizedException
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
                            @QueryParam("app_secret") @RequestParam("app_secret") String appSecret) {
        appService.verifyApp(appId, appSecret);
        if (!verifyToken(token)) return null;
        UserProfile userProfile = jwtService.decodeProfile(token);
        Tenant tenant = tenantService.get(userProfile.tenantId);
        if (tenant == null) {
            logger.error("tenant[{}] is not found", userProfile.tenantId);
            throw new NotAuthorizedException("租户不存在");
        }
        return tenant;
    }

    /**
     * 刷新token
     * 认证失败抛出异常NotAuthorizedException
     *
     * @param request 刷新请求
     * @return 成功返回新token，否则返回null
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("refreshToken")
    @PostMapping("refreshToken")
    @EnableWebLogAspect
    public String refreshToken(@RequestBody RefreshTokenRequest request) {
        if (request == null) throw new BadRequestException("刷新token请求为空");
        request.check();
        appService.verifyApp(request.appId, request.appSecret);
        if (!verifyToken(request.token)) return null;
        String ticket = tokenService.getTicket(request.token);
        if (StringUtils.isEmpty(ticket)) {
            logger.error("cached ticket is expired");
            throw new NotAuthorizedException("缓存ticket过期");
        }
        UserProfile userProfile = jwtService.decodeProfile(ticket);
        tokenService.removeToken(request.token);
        ticketService.removeToken(userProfile.id, request.token);
        String newToken = tokenService.buildToken(userProfile, Constants.CACHE_TOKEN_EXPIRED_TIME_MS);
        tokenService.putToken(newToken, ticket);
        ticketService.addToken(userProfile.id, newToken);
        ticketService.putTicket(userProfile.id, ticket);
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
    @Path("removeActivity")
    @PostMapping("removeActivity")
    @EnableWebLogAspect
    public boolean removeActivity(@RequestBody RemoveActivityRequest request) {
        if (request == null) throw new BadRequestException("移除活动记录请求为空");
        request.check();
        appService.verifyApp(request.appId, request.appSecret);
        return activityService.remove(request.sessionId, request.appId);
    }

    /**
     * 获取用户角色列表
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
        if (StringUtils.isEmpty(userId)) throw new BadRequestException("用户ID为空");
        appService.verifyApp(appId, appSecret);
        return userRoleService.getRolesByAppUser(appId, userId);
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
        if (request == null) throw new BadRequestException("角色权限请求为空");
        request.check();
        appService.verifyApp(request.appId, request.appSecret);
        RolePermissionSearchRequest searchRequest = new RolePermissionSearchRequest();
        searchRequest.roleIds = request.roleIds;
        searchRequest.current = Constants.DEFAULT_CURRENT;
        searchRequest.size = 100;
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
                          @Context HttpServletResponse httpResponse) {
        appService.verifyApp(appId, appSecret);
        String ticket = CookieUtils.getCookie(httpRequest, Constants.COOKIE_TICKET);
        if (StringUtils.isEmpty(ticket)) {
            CookieUtils.removeCookie(Constants.COOKIE_TOKEN, "/", httpResponse);
            logger.error("ticket is not found from cookies");
            throw new IllegalStateException("尚未登录");
        }
        removeTicketResources(ticket);
        SessionUtils.removeResources(httpRequest);
        CookieUtils.removeCookie(Constants.COOKIE_TOKEN, "/", httpResponse);
        CookieUtils.removeCookie(Constants.COOKIE_TICKET, "/", httpResponse);
        UserProfile userProfile = jwtService.decodeProfile(ticket);
        if (userProfile != null) activityService.remove(userProfile.id);
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
        String ticket = CookieUtils.getCookie(httpRequest, Constants.COOKIE_TICKET);
        if (StringUtils.isNotEmpty(ticket)) {
            logger.info("logged in");
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
        searchRequest.username = request.username.trim();
        searchRequest.current = 1;
        searchRequest.size = 1;
        Pager<User> pager = userService.search(searchRequest);
        if (pager == null || pager.total < 1 || pager.records.isEmpty()) {
            logger.error("user is not found for username[{}]", request.username);
            throw new NotFoundException("用户不存在");
        }
        User user = pager.records.get(0);
        if (!user.password.equals(DigestUtils.md5Hex(request.password.trim()))) {
            logger.error("password are not correct");
            throw new BadRequestException("密码不正确");
        }
        if (user.disabled) {
            logger.error("user is disabled");
            throw new IllegalStateException("用户处于禁用状态");
        }
        UserProfile userProfile = new UserProfile();
        userProfile.setId(RandomID.build()).setUserId(user.id).setTenantId(user.tenantId);
        ticket = ticketService.buildTicket(userProfile, Constants.COOKIE_TICKET_EXPIRED_TIME_MS);
        ticketService.putTicket(userProfile.id, ticket);
        CookieUtils.setCookie(Constants.COOKIE_TICKET, ticket, "/", true, httpRequest, httpResponse);
        CookieUtils.setCookie(Constants.COOKIE_TOKEN, RandomID.build(), "/", false, httpRequest, httpResponse);
        return true;
    }

    /**
     * 验证token
     *
     * @param token 令牌
     * @return 成功返回true，否则返回false
     */
    private boolean verifyToken(String token) {
        if (StringUtils.isEmpty(token)) {
            logger.error("token is empty");
            return false;
        }
        if (!tokenService.verifyToken(token)) {
            logger.error("verify token failed");
            removeTokenResources(token);
            return false;
        }
        return true;
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
        UserProfile userProfile = jwtService.decodeProfile(ticket);
        if (userProfile == null) {
            logger.error("decode profile failed from ticket");
            removeTicketResources(ticket);
            throw new NotAuthorizedException("非法ticket");
        }
        String cachedTicket = ticketService.getTicket(userProfile.id);
        if (StringUtils.isEmpty(cachedTicket) || !ticket.equals(cachedTicket)) {
            logger.error("cached ticket and provided ticket are not consistent");
            removeTicketResources(ticket);
            throw new NotAuthorizedException("ticket缓存失效");
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
        UserProfile userProfile = jwtService.decodeProfile(ticket);
        if (userProfile == null) return;
        userService.removeUserProfile(userProfile.id);
    }

    /**
     * 移除token相关资源
     *
     * @param token 令牌
     */
    private void removeTokenResources(String token) {
        tokenService.removeToken(token);
        UserProfile userProfile = jwtService.decodeProfile(token);
        if (userProfile == null) return;
        ticketService.removeToken(userProfile.id, token);
    }
}
