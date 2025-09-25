package xin.manong.hylian.server.service;

import xin.manong.hylian.server.wechat.AccessToken;
import xin.manong.hylian.server.wechat.QRCodeGenerateRequest;

import java.util.Map;

/**
 * 微信服务接口
 *
 * @author frankcl
 * @date 2025-05-29 20:20:58
 */
public interface WechatService {

    /**
     * 生成小程序码
     *
     * @param codeKey 小程序码key
     * @param request 小程序码生成请求
     * @return 小程序码图片
     */
    String generateMiniCode(String codeKey, QRCodeGenerateRequest request);

    /**
     * 获取AccessToken
     *
     * @return AccessToken
     */
    AccessToken getAccessToken();

    /**
     * 获取openid
     * @param code 授权码
     * @return openid
     */
    String getOpenid(String code);

    /**
     * 获取手机号
     *
     * @param phoneCode 授权码
     * @return 手机号
     */
    String getPhoneNumber(String phoneCode);

    /**
     * 发送通知消息
     *
     * @param openid 小程序openid
     * @param templateId 消息模板ID
     * @param messageBody 消息体
     * @return 发送成功返回true，否则返回false
     */
    boolean sendMessage(String openid, String templateId, Map<String, Object> messageBody);

    /**
     * 通知管理员
     *
     * @param templateId 消息模板ID
     * @param messageBody 消息体
     */
    void notifyAdmin(String templateId, Map<String, Object> messageBody);
}
