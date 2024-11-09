package xin.manong.hylian.server.controller;

import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.hylian.client.core.HTTPExecutor;
import xin.manong.hylian.model.User;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.config.ServerConfig;
import xin.manong.hylian.server.model.UserProfile;
import xin.manong.hylian.server.service.TicketService;
import xin.manong.hylian.server.util.AvatarUtils;
import xin.manong.hylian.server.util.CookieUtils;
import xin.manong.hylian.server.websocket.QRCodeWebSocket;
import xin.manong.hylian.server.wechat.*;
import xin.manong.hylian.server.controller.response.WechatLoginResponse;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.model.QRCode;
import xin.manong.hylian.server.model.Wechat;
import xin.manong.hylian.server.service.QRCodeService;
import xin.manong.hylian.server.service.UserService;
import xin.manong.hylian.server.service.WechatService;
import xin.manong.hylian.server.util.AppSecretUtils;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestFormat;
import xin.manong.weapon.base.util.FileUtil;
import xin.manong.weapon.base.util.RandomID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信控制器
 *
 * @author frankcl
 * @date 2024-11-04 11:18:22
 */
@RestController
@Controller
@Path("/api/wechat")
@RequestMapping("/api/wechat")
public class WechatController {

    private static final Logger logger = LoggerFactory.getLogger(WechatController.class);

    private static final String WECHAT_BASE_URL = "https://api.weixin.qq.com/";
    private static final String WECHAT_PATH_TOKEN = "cgi-bin/token";
    private static final String WECHAT_PATH_WXA_CODE = "wxa/getwxacodeunlimit";
    private static final String WECHAT_PATH_CODE_TO_SESSION = "sns/jscode2session";

    private static final String PARAM_KEY_APP_ID = "appid";
    private static final String PARAM_KEY_APP_SECRET = "secret";
    private static final String PARAM_KEY_JS_CODE = "js_code";
    private static final String PARAM_KEY_ACCESS_TOKEN = "access_token";
    private static final String PARAM_KEY_PAGE = "page";
    private static final String PARAM_KEY_SCENE = "scene";
    private static final String PARAM_KEY_CHECK_PATH = "check_path";
    private static final String PARAM_KEY_VERSION = "env_version";
    private static final String PARAM_KEY_GRANT_TYPE = "grant_type";

    private static final String GRANT_TYPE_CLIENT_CREDENTIAL = "client_credential";
    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    private static final String WX_LOGIN_PAGE = "pages/login/index";
    private static final String WX_SCENE_FORMAT = "key=%s";
    private static final String WX_VERSION = "develop";
    private static final String DEFAULT_PASSWORD = "123456";

    protected AccessToken accessToken;
    protected Wechat wechatMini;
    @Resource
    protected UserService userService;
    @Resource
    protected WechatService wechatService;
    @Resource
    protected QRCodeService qrCodeService;
    @Resource
    protected TicketService ticketService;
    @Resource
    private ServerConfig serverConfig;
    @Resource
    private OSSClient ossClient;

    /**
     * 生成小程序码
     *
     * @return 小程序码图片
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("code/generate")
    @GetMapping("code/generate")
    public QRCodeImage generateQRCode() {
        AccessToken accessToken = getAccessToken();
        String qrCodeKey = AppSecretUtils.buildSecret(12);
        while (qrCodeService.getByKey(qrCodeKey) != null) qrCodeKey = AppSecretUtils.buildSecret(12);
        String requestURL = String.format("%s%s?%s=%s", WECHAT_BASE_URL, WECHAT_PATH_WXA_CODE,
                PARAM_KEY_ACCESS_TOKEN, accessToken.token);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(PARAM_KEY_PAGE, WX_LOGIN_PAGE);
        requestBody.put(PARAM_KEY_CHECK_PATH, false);
        requestBody.put(PARAM_KEY_SCENE, String.format(WX_SCENE_FORMAT, qrCodeKey));
        requestBody.put(PARAM_KEY_VERSION, WX_VERSION);
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, requestBody);
        byte[] bytes = HTTPExecutor.executeRaw(httpRequest);
        if (bytes == null) throw new InternalServerErrorException("生成小程序码错误");
        String image = String.format("data:image/png;base64,%s", Base64.getEncoder().encodeToString(bytes));
        QRCode qrCode = new QRCode();
        qrCode.key = qrCodeKey;
        qrCode.status = 0;
        if (!qrCodeService.add(qrCode)) throw new InternalServerErrorException("添加小程序码记录失败");
        QRCodeImage qrCodeImage = new QRCodeImage();
        qrCodeImage.key = qrCodeKey;
        qrCodeImage.image = image;
        return qrCodeImage;
    }

    /**
     * 更新小程序码
     *
     * @param request 更新请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("code/update")
    @PostMapping("code/update")
    public boolean updateQRCode(@RequestBody QRCodeUpdateRequest request) throws IOException {
        if (request == null) throw new BadRequestException("更新请求为空");
        request.check();
        QRCode updateCode = Converter.convert(request);
        boolean success = qrCodeService.updateByKey(updateCode);
        if (success) QRCodeWebSocket.sendMessage(updateCode.key, JSON.toJSONString(updateCode));
        return success;
    }

    /**
     * 判断微信用户是否存在
     *
     * @param code 微信授权码
     * @return 存在返回true，否则返回false
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("user/exists")
    @GetMapping("user/exists")
    public boolean userExists(@QueryParam("code") String code) {
        if (StringUtils.isEmpty(code)) throw new BadRequestException("参数code缺失");
        String openid = getOpenid(code);
        return userService.getByWxUid(openid) != null;
    }

    /**
     * 上传头像
     *
     * @param fileDetail 文件信息
     * @param fileInputStream 文件流
     * @return 成功返回上传文件地址，否则抛出异常
     */
    @POST
    @Path("user/uploadAvatar")
    @PostMapping("user/uploadAvatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String uploadAvatar(@FormDataParam("file") FormDataContentDisposition fileDetail,
                               @FormDataParam("file") final InputStream fileInputStream) {
        return AvatarUtils.uploadAvatar(fileDetail, fileInputStream, ossClient, serverConfig);
    }

    /**
     * 微信账号授权认证
     *
     * @param request 授权认证请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("user/authorize")
    @PostMapping("user/authorize")
    public boolean authorize(@RequestBody AuthorizeRequest request) throws IOException {
        if (request == null) throw new BadRequestException("微信登录请求为空");
        request.check();
        QRCode qrCode = new QRCode();
        qrCode.key = request.key;
        try {
            String openid = getOpenid(request.code);
            qrCode.openid = openid;
            User user = userService.getByWxUid(openid);
            if (user == null) {
                addWechatUser(request.user, openid);
                user = userService.getByWxUid(openid);
            }
            qrCode.status = QRCode.STATUS_AUTHORIZED;
            if (!qrCodeService.updateByKey(qrCode)) logger.warn("update QRCode authorize status failed");
            if (user.disabled) throw new InternalServerErrorException("账号尚未审核通过，请联系管理员");
            return true;
        } catch (Exception e) {
            qrCode.status = QRCode.STATUS_ERROR;
            qrCode.message = e.getMessage();
            if (!qrCodeService.updateByKey(qrCode)) logger.warn("update QRCode error status failed");
            throw e;
        } finally {
            QRCodeWebSocket.sendMessage(qrCode.key, JSON.toJSONString(qrCode));
        }
    }

    /**
     * 微信扫码登录
     *
     * @param key 小程序码key
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @return 成功返回true，否则返回false
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("login")
    @GetMapping("login")
    public boolean login(@QueryParam("key") String key,
                         @Context HttpServletRequest httpRequest,
                         @Context HttpServletResponse httpResponse) {
        if (StringUtils.isEmpty(key)) throw new BadRequestException("小程序码key为空");
        QRCode qrCode = qrCodeService.getByKey(key);
        if (qrCode == null) throw new NotFoundException("小程序码已过期");
        if (StringUtils.isEmpty(qrCode.openid) || qrCode.status != QRCode.STATUS_AUTHORIZED) {
            throw new IllegalStateException("微信账号未授权");
        }
        User user = userService.getByWxUid(qrCode.openid);
        if (user == null) throw new IllegalStateException("用户未绑定微信账号");
        if (user.disabled) throw new IllegalStateException("账号尚未启用，请联系管理员");
        UserProfile userProfile = new UserProfile();
        userProfile.setId(RandomID.build()).setUserId(user.id);
        String ticket = ticketService.buildTicket(userProfile, Constants.COOKIE_TICKET_EXPIRED_TIME_MS);
        ticketService.putTicket(userProfile.id, ticket);
        CookieUtils.setCookie(Constants.COOKIE_TICKET, ticket, "/", true, httpRequest, httpResponse);
        CookieUtils.setCookie(Constants.COOKIE_TOKEN, RandomID.build(), "/", false, httpRequest, httpResponse);
        return true;
    }

    /**
     * 添加微信用户信息
     *
     * @param wechatUser 微信用户信息
     * @param openId 微信小程序openId
     */
    private void addWechatUser(WechatUser wechatUser, String openId) {
        if (wechatUser == null) throw new BadRequestException("微信用户信息缺失");
        User user = new User();
        user.id = RandomID.build();
        user.username = String.format("openid_%s", openId);
        user.name = wechatUser.nickName;
        user.tenantId = serverConfig.defaultTenant;
        user.password = DigestUtils.md5Hex(DEFAULT_PASSWORD);
        user.disabled = true;
        user.wxUid = openId;
        String avatarURL = downloadAvatar(wechatUser);
        if (StringUtils.isNotEmpty(avatarURL)) user.avatar = avatarURL;
        if (!userService.add(user)) throw new InternalServerErrorException("添加微信用户失败");
    }

    /**
     * 下载微信头像
     *
     * @param wechatUser 微信用户信息
     * @return 成功返回OSS地址，否则返回null
     */
    private String downloadAvatar(WechatUser wechatUser) {
        if (StringUtils.isEmpty(wechatUser.avatar)) return null;
        URL avatarURL;
        try {
            avatarURL = new URL(wechatUser.avatar);
        } catch (MalformedURLException e) {
            logger.error("invalid wechat avatar[{}]", wechatUser.avatar);
            return null;
        }
        HttpRequest httpRequest = HttpRequest.buildGetRequest(wechatUser.avatar, null);
        byte[] bytes = HTTPExecutor.executeRaw(httpRequest);
        if (bytes == null || bytes.length == 0) {
            logger.error("download wechat avatar[{}] failed", wechatUser.avatar);
            return null;
        }
        String suffix = FileUtil.getFileSuffix(avatarURL.getPath());
        String ossKey = String.format("%s%s%s", serverConfig.ossBaseDirectory,
                Constants.TEMP_AVATAR_DIR, RandomID.build());
        if (StringUtils.isNotEmpty(suffix)) ossKey = String.format("%s.%s", ossKey, suffix);
        if (!ossClient.putObject(serverConfig.ossBucket, ossKey, bytes)) {
            logger.error("save oss wechat avatar[{}] failed", wechatUser.avatar);
            return null;
        }
        OSSMeta ossMeta = new OSSMeta();
        ossMeta.region = serverConfig.ossRegion;
        ossMeta.bucket = serverConfig.ossBucket;
        ossMeta.key = ossKey;
        return OSSClient.buildURL(ossMeta);
    }

    /**
     * 获取小程序openid
     *
     * @param code 微信授权码
     * @return 小程序openid
     */
    private String getOpenid(String code) {
        String requestURL = String.format("%s%s", WECHAT_BASE_URL, WECHAT_PATH_CODE_TO_SESSION);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(PARAM_KEY_APP_ID, getMiniAppId());
        paramMap.put(PARAM_KEY_APP_SECRET, getMiniAppSecret());
        paramMap.put(PARAM_KEY_GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);
        paramMap.put(PARAM_KEY_JS_CODE, code);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        String body = HTTPExecutor.execute(httpRequest);
        if (StringUtils.isEmpty(body)) throw new InternalServerErrorException("微信登录失败");
        WechatLoginResponse response = JSON.parseObject(body, WechatLoginResponse.class);
        return response.openid;
    }

    /**
     * 获取AccessToken
     *
     * @return 访问令牌
     */
    private AccessToken getAccessToken() {
        if (accessToken != null && accessToken.expiresIn - System.currentTimeMillis() > 60000) return accessToken;
        String requestURL = String.format("%s%s", WECHAT_BASE_URL, WECHAT_PATH_TOKEN);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(PARAM_KEY_APP_ID, getMiniAppId());
        paramMap.put(PARAM_KEY_APP_SECRET, getMiniAppSecret());
        paramMap.put(PARAM_KEY_GRANT_TYPE, GRANT_TYPE_CLIENT_CREDENTIAL);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        String body = HTTPExecutor.execute(httpRequest);
        if (StringUtils.isEmpty(body)) throw new InternalServerErrorException("获取AccessToken失败");
        accessToken = JSON.parseObject(body, AccessToken.class);
        accessToken.expiresIn = System.currentTimeMillis() + accessToken.expiresIn * 1000L;
        return accessToken;
    }

    /**
     * 获取小程序应用ID
     *
     * @return 小程序应用ID
     */
    private String getMiniAppId() {
        if (wechatMini == null) wechatMini = wechatService.getMiniWeChat();
        return wechatMini.appId;
    }

    /**
     * 获取小程序应用秘钥
     *
     * @return 小程序应用秘钥
     */
    private String getMiniAppSecret() {
        if (wechatMini == null) wechatMini = wechatService.getMiniWeChat();
        return wechatMini.appSecret;
    }
}
