package xin.manong.hylian.server.wechat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * 微信账号认证请求
 *
 * @author frankcl
 * @date 2024-10-16 16:29:29
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthorizeRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizeRequest.class);

    /**
     * 微信小程序授权码
     */
    @JsonProperty("code")
    public String code;

    /**
     * 微信小程序码key
     */
    @JsonProperty("key")
    public String key;

    /**
     * 微信用户信息
     */
    @JsonProperty("user")
    public WechatUser user;

    /**
     * 检测有效性
     * 无效信息抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(code)) {
            logger.error("code is empty");
            throw new BadRequestException("微信小程序凭证code为空");
        }
        if (StringUtils.isEmpty(key)) {
            logger.error("key is empty");
            throw new BadRequestException("微信小程序码key为空");
        }
    }
}
