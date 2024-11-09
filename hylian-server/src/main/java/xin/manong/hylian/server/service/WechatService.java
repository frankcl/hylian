package xin.manong.hylian.server.service;

import xin.manong.hylian.server.model.Wechat;

/**
 * 微信服务
 *
 * @author frankcl
 * @date 2024-11-04 11:41:51
 */
public interface WechatService {

    /**
     * 获取小程序配置
     *
     * @return 小程序配置
     */
    Wechat getMiniWeChat();
}
