package xin.manong.hylian.server.controller.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * 登录请求
 *
 * @author frankcl
 * @date 2023-09-05 13:51:00
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(LoginRequest.class);

    /**
     * 用户名
     */
    @JsonProperty("username")
    public String username;
    /**
     * 密码
     */
    @JsonProperty("password")
    public String password;
    /**
     * 验证码
     */
    @JsonProperty("captcha")
    public String captcha;

    /**
     * 检测有效性，无效请求抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(username)) {
            logger.error("Username is empty");
            throw new BadRequestException("用户名为空");
        }
        if (StringUtils.isEmpty(password)) {
            logger.error("Password is empty");
            throw new BadRequestException("密码为空");
        }
        if (StringUtils.isEmpty(captcha)) {
            logger.error("Captcha is empty");
            throw new BadRequestException("验证码为空");
        }
    }
}
