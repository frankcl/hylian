package xin.manong.hylian.server.wechat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 微信账号注册请求
 *
 * @author frankcl
 * @date 2024-10-16 16:29:29
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterRequest extends AuthorizeRequest {

    private static final Logger logger = LoggerFactory.getLogger(RegisterRequest.class);

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
        super.check();
        if (user == null) {
            logger.error("wechat user is null");
            throw new BadRequestException("微信账号信息为空");
        }
    }
}
