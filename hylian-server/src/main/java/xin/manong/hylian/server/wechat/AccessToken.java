package xin.manong.hylian.server.wechat;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 微信AccessToken
 *
 * @author frankcl
 * @date 2024-11-04 11:15:43
 */
public class AccessToken {

    /**
     * 微信口令
     */
    @JsonProperty("access_token")
    @JSONField(name = "access_token")
    public String token;
    /**
     * 过期时间，单位：秒
     */
    @JsonProperty("expires_in")
    @JSONField(name = "expires_in")
    public Long expiresIn;
}
