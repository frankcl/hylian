package xin.manong.hylian.server.wechat;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 微信用户信息
 *
 * @author frankcl
 * @date 2024-10-16 16:29:29
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WechatUser implements Serializable {

    /**
     * 昵称
     */
    @JsonProperty("nickName")
    @JSONField(name = "nickName")
    public String nickName;
    /**
     * 头像
     */
    @JsonProperty("avatarUrl")
    @JSONField(name = "avatarUrl")
    public String avatar;

    /**
     * 电话号码
     */
    @JsonProperty("phone")
    @JSONField(name = "phone")
    public String phone;
}
