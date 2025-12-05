package xin.manong.hylian.server.service.impl;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import jakarta.ws.rs.InternalServerErrorException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.hylian.client.config.HylianClientConfig;
import xin.manong.hylian.client.core.HTTPExecutor;
import xin.manong.hylian.client.core.HTTPResponse;
import xin.manong.hylian.model.User;
import xin.manong.hylian.server.config.ServerConfig;
import xin.manong.hylian.server.controller.response.WechatLoginResponse;
import xin.manong.hylian.server.service.AppUserService;
import xin.manong.hylian.server.service.WechatService;
import xin.manong.hylian.server.wechat.AccessToken;
import xin.manong.hylian.server.wechat.MessageResponse;
import xin.manong.hylian.server.wechat.PhoneResponse;
import xin.manong.hylian.server.wechat.QRCodeGenerateRequest;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestFormat;
import xin.manong.weapon.spring.boot.etcd.WatchValue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信服务实现
 *
 * @author frankcl
 * @date 2025-05-29 20:46:22
 */
@Service
public class WechatServiceImpl implements WechatService {

    private static final Logger logger = LoggerFactory.getLogger(WechatServiceImpl.class);

    private static final String WECHAT_BASE_URL = "https://api.weixin.qq.com/";
    private static final String WECHAT_PATH_TOKEN = "cgi-bin/stable_token";
    private static final String WECHAT_PATH_WXA_CODE = "wxa/getwxacodeunlimit";
    private static final String WECHAT_PATH_CODE_TO_SESSION = "sns/jscode2session";
    private static final String WECHAT_PATH_PHONE_NUMBER = "wxa/business/getuserphonenumber";
    private static final String WECHAT_PATH_SEND_MESSAGE = "cgi-bin/message/subscribe/send";

    private static final String PARAM_KEY_APP_ID = "appid";
    private static final String PARAM_KEY_APP_SECRET = "secret";
    private static final String PARAM_KEY_JS_CODE = "js_code";
    private static final String PARAM_KEY_CODE = "code";
    private static final String PARAM_KEY_ACCESS_TOKEN = "access_token";
    private static final String PARAM_KEY_PAGE = "page";
    private static final String PARAM_KEY_SCENE = "scene";
    private static final String PARAM_KEY_CHECK_PATH = "check_path";
    private static final String PARAM_KEY_VERSION = "env_version";
    private static final String PARAM_KEY_GRANT_TYPE = "grant_type";
    private static final String PARAM_KEY_MSG_USER = "touser";
    private static final String PARAM_KEY_MSG_TEMPLATE_ID = "template_id";
    private static final String PARAM_KEY_MSG_DATA = "data";
    private static final String PARAM_KEY_MSG_STATE = "miniprogram_state";
    private static final String PARAM_KEY_MSG_LANG = "lang";

    private static final String GRANT_TYPE_CLIENT_CREDENTIAL = "client_credential";
    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    private static final String WX_SCENE_FORMAT = "key=%s";
    public static final String OPENID_PREFIX = "openid_";

    private AccessToken accessToken;
    @Resource
    private HylianClientConfig hylianClientConfig;
    @Resource
    private ServerConfig serverConfig;
    @Resource
    private AppUserService appUserService;
    @WatchValue(namespace = "hylian", key = "wechat/app_id")
    private String appId;
    @WatchValue(namespace = "hylian", key = "wechat/app_secret")
    private String appSecret;

    @Override
    public String generateMiniCode(String codeKey, QRCodeGenerateRequest request) {
        AccessToken accessToken = getAccessToken();
        String pageURL = request.category == QRCodeGenerateRequest.CATEGORY_LOGIN ?
                serverConfig.wechatPageLogin : serverConfig.wechatPageBind;
        String requestURL = String.format("%s%s?%s=%s", WECHAT_BASE_URL, WECHAT_PATH_WXA_CODE,
                PARAM_KEY_ACCESS_TOKEN, accessToken.token);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(PARAM_KEY_PAGE, pageURL);
        requestBody.put(PARAM_KEY_CHECK_PATH, false);
        requestBody.put(PARAM_KEY_SCENE, String.format(WX_SCENE_FORMAT, codeKey));
        requestBody.put(PARAM_KEY_VERSION, StringUtils.isEmpty(request.wxVersion) ?
                serverConfig.wxVersion : request.wxVersion);
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, requestBody);
        HTTPResponse httpResponse = HTTPExecutor.executeRequest(httpRequest);
        if (httpResponse == null || !httpResponse.isImage()) {
            logger.error("Generate wechat mini code error: {}", httpResponse == null ?
                    "未知" : new String(httpResponse.content, StandardCharsets.UTF_8));
            throw new InternalServerErrorException("生成小程序码错误");
        }
        return String.format("data:image/png;base64,%s",
                Base64.getEncoder().encodeToString(httpResponse.content));
    }

    @Override
    public AccessToken getAccessToken() {
        if (accessToken != null && accessToken.expiresIn - System.currentTimeMillis() > 60000) return accessToken;
        String requestURL = String.format("%s%s", WECHAT_BASE_URL, WECHAT_PATH_TOKEN);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(PARAM_KEY_APP_ID, appId);
        paramMap.put(PARAM_KEY_APP_SECRET, appSecret);
        paramMap.put(PARAM_KEY_GRANT_TYPE, GRANT_TYPE_CLIENT_CREDENTIAL);
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, paramMap);
        String body = HTTPExecutor.execute(httpRequest);
        if (StringUtils.isEmpty(body)) throw new InternalServerErrorException("获取AccessToken失败");
        accessToken = JSON.parseObject(body, AccessToken.class);
        accessToken.expiresIn = System.currentTimeMillis() + accessToken.expiresIn * 1000L;
        return accessToken;
    }

    @Override
    public String getOpenid(String code) {
        String requestURL = String.format("%s%s", WECHAT_BASE_URL, WECHAT_PATH_CODE_TO_SESSION);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(PARAM_KEY_APP_ID, appId);
        paramMap.put(PARAM_KEY_APP_SECRET, appSecret);
        paramMap.put(PARAM_KEY_GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);
        paramMap.put(PARAM_KEY_JS_CODE, code);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        String body = HTTPExecutor.execute(httpRequest);
        if (StringUtils.isEmpty(body)) throw new InternalServerErrorException("获取微信小程序openid失败");
        WechatLoginResponse response = JSON.parseObject(body, WechatLoginResponse.class);
        return response.openid;
    }

    @Override
    public String getPhoneNumber(String phoneCode) {
        AccessToken accessToken = getAccessToken();
        String requestURL = String.format("%s%s?%s=%s", WECHAT_BASE_URL,
                WECHAT_PATH_PHONE_NUMBER, PARAM_KEY_ACCESS_TOKEN, accessToken.token);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(PARAM_KEY_CODE, phoneCode);
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, paramMap);
        String body = HTTPExecutor.execute(httpRequest);
        if (StringUtils.isEmpty(body)) throw new InternalServerErrorException("获取电话号码失败");
        PhoneResponse response = JSON.parseObject(body, PhoneResponse.class);
        if (response.code != 0) {
            logger.error("get phone number failed, code:{}, message:{}", response.code, response.message);
            return null;
        }
        return response.phoneInfo.purePhoneNumber;
    }

    @Override
    public boolean sendMessage(String openid, String templateId, Map<String, Object> messageBody) {
        String requestURL = String.format("%s%s?%s=%s", WECHAT_BASE_URL, WECHAT_PATH_SEND_MESSAGE,
                PARAM_KEY_ACCESS_TOKEN, accessToken.token);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(PARAM_KEY_MSG_TEMPLATE_ID, templateId);
        paramMap.put(PARAM_KEY_MSG_USER, openid);
        paramMap.put(PARAM_KEY_MSG_STATE, serverConfig.miniVersion);
        paramMap.put(PARAM_KEY_MSG_LANG, "zh_CN");
        paramMap.put(PARAM_KEY_MSG_DATA, messageBody);
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, paramMap);
        String body = HTTPExecutor.execute(httpRequest);
        if (StringUtils.isEmpty(body)) throw new InternalServerErrorException("发送微信通知失败");
        MessageResponse response = JSON.parseObject(body, MessageResponse.class);
        if (response.code != 0) {
            logger.error("Send wechat message failed, code:{}, message:{}", response.code, response.message);
            return false;
        }
        return true;
    }

    @Override
    public void notifyAdmin(String templateId, Map<String, Object> messageBody) {
        List<User> admins = appUserService.getUsersByApp(hylianClientConfig.appId);
        for (User admin : admins) {
            if (StringUtils.isEmpty(admin.wxOpenid)) continue;
            String openid = admin.wxOpenid;
            if (openid.startsWith(OPENID_PREFIX)) openid = openid.substring(OPENID_PREFIX.length());
            if (sendMessage(openid, templateId, messageBody)) continue;
            logger.warn("Send message to admin:{} failed", admin.name);
        }
    }
}
