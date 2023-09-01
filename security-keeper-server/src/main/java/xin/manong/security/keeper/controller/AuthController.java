package xin.manong.security.keeper.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.security.keeper.model.App;
import xin.manong.security.keeper.model.Tenant;
import xin.manong.security.keeper.model.User;
import xin.manong.security.keeper.request.LoginRequest;
import xin.manong.security.keeper.request.RefreshTokenRequest;
import xin.manong.security.keeper.service.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.net.URL;
import java.net.URLEncoder;

/**
 * 登录认证控制器
 *
 * @author frankcl
 * @date 2023-08-31 15:33:28
 */
@RestController
@Controller
@Path("/security/auth")
@RequestMapping("/security/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private static final String CHARSET_UTF8 = "UTF-8";
    private static final String COOKIE_TICKET = "TICKET";

    private static final String PATH_LOGIN = "/security/login";

    private static final String PARAM_APP_ID = "app_id";
    private static final String PARAM_APP_SECRET = "app_secret";
    private static final String PARAM_TOKEN = "token";
    private static final String PARAM_CODE = "code";
    private static final String PARAM_REDIRECT_URL = "redirect_url";

    private static final Long TOKEN_EXPIRED_TIME = 300000L;
    private static final Long TICKET_EXPIRED_TIME = 86400000L;

    @Resource
    protected JWTService jwtService;
    @Resource
    protected CodeService codeService;
    @Resource
    protected CookieService cookieService;
    @Resource
    protected AppService appService;
    @Resource
    protected HTTPService httpService;

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
    @Path("applyCode")
    @GetMapping("applyCode")
    public void applyCode(@QueryParam("app_id")  String appId,
                          @QueryParam("redirect_url") String redirectURL,
                          @Context HttpServletRequest httpRequest,
                          @Context HttpServletResponse httpResponse) throws Exception {
        if (StringUtils.isEmpty(appId)) {
            logger.error("app id is empty");
            throw new BadRequestException("应用ID为空");
        }
        if (StringUtils.isEmpty(redirectURL)) {
            logger.error("redirect url is empty");
            throw new BadRequestException("重定向URL为空");
        }
        String ticket = cookieService.getCookie(httpRequest, COOKIE_TICKET);
        if (StringUtils.isEmpty(ticket) || !jwtService.verifyTicket(ticket)) {
            if (ticket != null) cookieService.removeCookie(COOKIE_TICKET, "/", httpResponse);
            httpResponse.sendRedirect(String.format("%s?%s=%s", PATH_LOGIN, PARAM_REDIRECT_URL,
                    URLEncoder.encode(redirectURL, CHARSET_UTF8)));
        }
        String code = codeService.createCode(ticket);
        String requestURL = httpService.getRequestURL(httpRequest);
        boolean hasQuery = !StringUtils.isEmpty(new URL(requestURL).getQuery());
        httpResponse.sendRedirect(String.format("%s%scode=%s", requestURL, hasQuery ? "&" : "?", code));
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
    @Path("getToken")
    @GetMapping("getToken")
    public String getToken(@QueryParam("code")  String code,
                           @QueryParam("app_id")  String appId,
                           @QueryParam("app_secret") String appSecret) {
        if (StringUtils.isEmpty(code)) {
            logger.error("code is empty");
            throw new BadRequestException("安全code为空");
        }
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
        if (!appSecret.equals(app.secret)) {
            logger.error("app secret[{}] is not correct");
            throw new RuntimeException("应用秘钥不正确");
        }
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
    @Path("getUser")
    @GetMapping("getUser")
    public User getUser(@QueryParam("token")  String token,
                        @QueryParam("app_id")  String appId,
                        @QueryParam("app_secret") String appSecret) {
        if (StringUtils.isEmpty(token)) {
            logger.error("token is empty");
            throw new BadRequestException("token为空");
        }
        if (StringUtils.isEmpty(appId)) {
            logger.error("app id is empty");
            throw new BadRequestException("应用ID为空");
        }
        if (StringUtils.isEmpty(appSecret)) {
            logger.error("app secret is empty");
            throw new BadRequestException("应用秘钥为空");
        }
        return null;
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
    @Path("getTenant")
    @GetMapping("getTenant")
    public Tenant getTenant(@QueryParam("token")  String token,
                            @QueryParam("app_id")  String appId,
                            @QueryParam("app_secret") String appSecret) {
        if (StringUtils.isEmpty(token)) {
            logger.error("token is empty");
            throw new BadRequestException("token为空");
        }
        if (StringUtils.isEmpty(appId)) {
            logger.error("app id is empty");
            throw new BadRequestException("应用ID为空");
        }
        if (StringUtils.isEmpty(appSecret)) {
            logger.error("app secret is empty");
            throw new BadRequestException("应用秘钥为空");
        }
        return null;
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
    @Path("getVendor")
    @GetMapping("getVendor")
    public Tenant getVendor(@QueryParam("token")  String token,
                            @QueryParam("app_id")  String appId,
                            @QueryParam("app_secret") String appSecret) {
        if (StringUtils.isEmpty(token)) {
            logger.error("token is empty");
            throw new BadRequestException("token为空");
        }
        if (StringUtils.isEmpty(appId)) {
            logger.error("app id is empty");
            throw new BadRequestException("应用ID为空");
        }
        if (StringUtils.isEmpty(appSecret)) {
            logger.error("app secret is empty");
            throw new BadRequestException("应用秘钥为空");
        }
        return null;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("refreshToken")
    @PostMapping("refreshToken")
    public String refreshToken(@RequestBody RefreshTokenRequest request) {
        if (request == null) {
            logger.error("refresh token request is null");
            throw new BadRequestException("刷新token请求为空");
        }
        request.check();
        return null;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("login")
    @PostMapping("login")
    public void login(@RequestBody LoginRequest request) {
        if (request == null) {
            logger.error("login request is null");
            throw new BadRequestException("登录请求为空");
        }
        request.check();
    }
}
