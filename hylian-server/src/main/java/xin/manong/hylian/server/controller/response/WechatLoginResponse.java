package xin.manong.hylian.server.controller.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * 微信登录响应
 *
 * @author frankcl
 * @date 2024-11-06 20:01:27
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WechatLoginResponse {

    /**
     * 微信用户唯一ID
     */
    @JsonProperty("openid")
    @JSONField(name = "openid")
    public String openid;
    /**
     * 微信登录会话key
     */
    @JsonProperty("session_key")
    @JSONField(name = "session_key")
    public String sessionKey;
}
