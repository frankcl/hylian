package xin.manong.hylian.server.controller;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
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
import xin.manong.hylian.server.service.WechatService;
import xin.manong.hylian.server.service.impl.WechatServiceImpl;
import xin.manong.hylian.server.util.AvatarUtils;
import xin.manong.hylian.client.util.CookieUtils;
import xin.manong.hylian.server.websocket.QRCodeWebSocket;
import xin.manong.hylian.server.wechat.*;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.model.QRCode;
import xin.manong.hylian.server.service.QRCodeService;
import xin.manong.hylian.server.service.UserService;
import xin.manong.hylian.server.util.AppSecretUtils;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.util.FileUtil;
import xin.manong.weapon.base.util.RandomID;
import xin.manong.weapon.spring.boot.etcd.WatchValueDisposableBean;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

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
public class WechatController extends WatchValueDisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(WechatController.class);

    private static final String DEFAULT_PASSWORD = "123456";

    @Resource
    private UserService userService;
    @Resource
    private QRCodeService qrCodeService;
    @Resource
    private TicketService ticketService;
    @Resource
    private WechatService wechatService;
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("code/generate")
    @GetMapping("code/generate")
    public QRCodeImage generateQRCode(@BeanParam QRCodeGenerateRequest request) {
        if (request == null) throw new BadRequestException("小程序码生成请求为空");
        request.check();
        String codeKey = AppSecretUtils.buildSecret(12);
        while (qrCodeService.getByKey(codeKey) != null) codeKey = AppSecretUtils.buildSecret(12);
        String image = wechatService.generateMiniCode(codeKey, request);
        QRCode qrCode = new QRCode();
        qrCode.key = codeKey;
        qrCode.status = QRCode.STATUS_WAIT;
        qrCode.userid = request.userid;
        if (!qrCodeService.add(qrCode)) throw new InternalServerErrorException("添加小程序码记录失败");
        QRCodeImage qrCodeImage = new QRCodeImage();
        qrCodeImage.key = codeKey;
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
        String openid = wechatService.getOpenid(code);
        return userService.getByWxOpenid(openid) != null;
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
     * 微信账号绑定
     *
     * @param request 账号绑定请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("user/bind")
    @PostMapping("user/bind")
    public boolean bind(@RequestBody UserBindRequest request) throws IOException {
        if (request == null) throw new BadRequestException("微信账号绑定请求为空");
        request.check();
        QRCode qrCode = qrCodeService.getByKey(request.key);
        if (qrCode == null) throw new IllegalStateException("小程序码已过期");
        try {
            String openid = wechatService.getOpenid(request.getCode());
            User user = userService.getByWxOpenid(openid);
            if (user != null) throw new IllegalStateException("微信账号已被绑定");
            user = userService.get(qrCode.userid);
            if (user == null) {
                logger.error("User:{} is not found", qrCode.userid);
                throw new IllegalStateException("未找到绑定用户");
            }
            User updateUser = new User();
            updateUser.id = qrCode.userid;
            updateUser.wxOpenid = openid;
            boolean success = userService.update(updateUser);
            qrCode.status = QRCode.STATUS_BIND;
            if (!qrCodeService.updateByKey(qrCode)) logger.warn("Update QRCode bind status failed");
            return success;
        } catch (Exception e) {
            qrCode.status = QRCode.STATUS_ERROR;
            qrCode.message = e.getMessage();
            if (!qrCodeService.updateByKey(qrCode)) logger.warn("Update bind QRCode:{} status failed", qrCode.key);
            throw e;
        } finally {
            QRCodeWebSocket.sendMessage(qrCode.key, JSON.toJSONString(qrCode));
        }
    }

    /**
     * 微信用户注册登录
     *
     * @param request 注册登录请求
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("user/registerLogin")
    @PostMapping("user/registerLogin")
    public boolean registerLogin(@RequestBody RegisterLoginRequest request,
                                 @Context HttpServletRequest httpRequest,
                                 @Context HttpServletResponse httpResponse) {
        if (request == null) throw new BadRequestException("微信注册登录请求为空");
        request.check();
        String openid = wechatService.getOpenid(request.code);
        User user = userService.getByWxOpenid(openid);
        if (user == null) {
            WechatUser wechatUser = new WechatUser();
            wechatUser.nickName = "微信用户";
            if (StringUtils.isNotEmpty(request.phoneCode)) {
                String phone = wechatService.getPhoneNumber(request.phoneCode);
                if (StringUtils.isNotEmpty(phone)) wechatUser.phone = phone;
            }
            user = addWechatUser(wechatUser, openid, false);
        }
        afterLogin(user, httpRequest, httpResponse);
        return true;
    }

    /**
     * 微信账号注册
     *
     * @param request 授权认证请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("user/register")
    @PostMapping("user/register")
    public boolean register(@RequestBody RegisterRequest request) throws IOException {
        if (request == null) throw new BadRequestException("微信注册请求为空");
        request.check();
        QRCode qrCode = new QRCode();
        qrCode.key = request.key;
        try {
            String openid = wechatService.getOpenid(request.code);
            qrCode.openid = openid;
            User user = userService.getByWxOpenid(openid);
            if (user != null) throw new IllegalStateException("微信账号已注册");
            addWechatUser(request.user, openid, true);
            qrCode.status = QRCode.STATUS_REGISTERED;
            if (!qrCodeService.updateByKey(qrCode)) logger.warn("Update QRCode register status failed");
            NoticeNewUser noticeNewUser = new NoticeNewUser(request.user.nickName, "微信");
            wechatService.notifyAdmin(serverConfig.wechatNoticeNewUser, noticeNewUser.toMap());
            return true;
        } catch (Exception e) {
            qrCode.status = QRCode.STATUS_ERROR;
            qrCode.message = e.getMessage();
            if (!qrCodeService.updateByKey(qrCode)) {
                logger.warn("Update QRCode:{} status failed when registering", qrCode.key);
            }
            throw e;
        } finally {
            QRCodeWebSocket.sendMessage(qrCode.key, JSON.toJSONString(qrCode));
        }
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
            String openid = wechatService.getOpenid(request.code);
            qrCode.openid = openid;
            User user = userService.getByWxOpenid(openid);
            if (user == null) throw new IllegalStateException("微信用户不存在");
            qrCode.status = QRCode.STATUS_AUTHORIZED;
            if (!qrCodeService.updateByKey(qrCode)) logger.warn("Update QRCode authorize status failed");
            return true;
        } catch (Exception e) {
            qrCode.status = QRCode.STATUS_ERROR;
            qrCode.message = e.getMessage();
            if (!qrCodeService.updateByKey(qrCode)) {
                logger.warn("Update QRCode:{} status failed when authorizing", qrCode.key);
            }
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
    @Path("user/login")
    @GetMapping("user/login")
    public boolean login(@QueryParam("key") String key,
                         @Context HttpServletRequest httpRequest,
                         @Context HttpServletResponse httpResponse) throws IOException {
        if (StringUtils.isEmpty(key)) throw new BadRequestException("小程序码key为空");
        QRCode qrCode = qrCodeService.getByKey(key);
        if (qrCode == null) throw new NotFoundException("小程序码已过期");
        try {
            if (StringUtils.isEmpty(qrCode.openid) || (qrCode.status != QRCode.STATUS_AUTHORIZED &&
                    qrCode.status != QRCode.STATUS_REGISTERED)) {
                throw new IllegalStateException("微信账号未授权");
            }
            User user = userService.getByWxOpenid(qrCode.openid);
            if (user == null) throw new IllegalStateException("用户未绑定微信账号");
            if (user.disabled) throw new IllegalStateException("账号尚未审核，请联系管理员");
            afterLogin(user, httpRequest, httpResponse);
            return true;
        } catch (Exception e) {
            qrCode.status = QRCode.STATUS_ERROR;
            qrCode.message = e.getMessage();
            if (!qrCodeService.updateByKey(qrCode)) logger.warn("Update QRCode:{} status failed when login", qrCode.key);
            QRCodeWebSocket.sendMessage(qrCode.key, JSON.toJSONString(qrCode));
            throw e;
        }
    }

    /**
     * 登录后处理
     *
     * @param user 登录用户
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     */
    private void afterLogin(User user,
                            HttpServletRequest httpRequest,
                            HttpServletResponse httpResponse) {
        UserProfile userProfile = new UserProfile();
        userProfile.setId(RandomID.build()).setUserId(user.id);
        String ticket = ticketService.buildTicket(userProfile, Constants.COOKIE_TICKET_EXPIRED_TIME_MS);
        ticketService.putTicket(userProfile.id, ticket);
        CookieUtils.setCookie(Constants.COOKIE_TICKET, ticket, "/",
                serverConfig.domain, true, httpRequest, httpResponse);
        CookieUtils.setCookie(Constants.COOKIE_TOKEN, RandomID.build(), "/",
                serverConfig.domain, false, httpRequest, httpResponse);
        httpResponse.addHeader(Constants.HEADER_TICKET, ticket);
    }

    /**
     * 添加微信用户信息
     *
     * @param wechatUser 微信用户信息
     * @param openId 微信小程序openId
     * @return 新用户信息
     */
    private User addWechatUser(WechatUser wechatUser, String openId, boolean disabled) {
        if (wechatUser == null) throw new BadRequestException("微信用户信息缺失");
        User user = new User();
        user.id = RandomID.build();
        user.username = String.format("%s%s", WechatServiceImpl.OPENID_PREFIX, openId);
        user.name = wechatUser.nickName;
        user.phone = wechatUser.phone;
        user.tenantId = serverConfig.defaultTenant;
        user.password = DigestUtils.md5Hex(DEFAULT_PASSWORD);
        user.disabled = disabled;
        user.wxOpenid = openId;
        user.registerMode = User.REGISTER_MODE_WECHAT;
        String avatarURL = downloadAvatar(wechatUser);
        if (StringUtils.isNotEmpty(avatarURL)) user.avatar = avatarURL;
        if (!userService.add(user)) throw new InternalServerErrorException("添加微信用户失败");
        return user;
    }

    /**
     * 下载微信头像
     *
     * @param wechatUser 微信用户信息
     * @return 成功返回OSS地址，否则返回null
     */
    private String downloadAvatar(WechatUser wechatUser) {
        if (!needDownload(wechatUser.avatar)) return wechatUser.avatar;
        URL avatarURL;
        try {
            avatarURL = new URL(wechatUser.avatar);
        } catch (MalformedURLException e) {
            logger.error("Invalid wechat avatar:{}", wechatUser.avatar);
            return null;
        }
        HttpRequest httpRequest = HttpRequest.buildGetRequest(wechatUser.avatar, null);
        byte[] bytes = HTTPExecutor.executeRaw(httpRequest);
        if (bytes == null || bytes.length == 0) {
            logger.error("Download wechat avatar:{} failed", wechatUser.avatar);
            return null;
        }
        String suffix = FileUtil.getFileSuffix(avatarURL.getPath());
        String ossKey = String.format("%s%s%s", serverConfig.ossBaseDirectory,
                Constants.TEMP_AVATAR_DIR, RandomID.build());
        if (StringUtils.isNotEmpty(suffix)) ossKey = String.format("%s.%s", ossKey, suffix);
        if (!ossClient.putObject(serverConfig.ossBucket, ossKey, bytes)) {
            logger.error("Save oss wechat avatar:{} failed", wechatUser.avatar);
            return null;
        }
        OSSMeta ossMeta = new OSSMeta();
        ossMeta.region = serverConfig.ossRegion;
        ossMeta.bucket = serverConfig.ossBucket;
        ossMeta.key = ossKey;
        return OSSClient.buildURL(ossMeta);
    }

    /**
     * 判断头像是否需要下载
     * OSS地址不需要下载
     *
     * @param avatarURL 头像URL
     * @return 需要下载返回true，否则返回false
     */
    private boolean needDownload(String avatarURL) {
        if (StringUtils.isEmpty(avatarURL)) return false;
        OSSMeta ossMeta = OSSClient.parseURL(avatarURL);
        return ossMeta == null || !ossMeta.region.equals(serverConfig.ossRegion);
    }
}
