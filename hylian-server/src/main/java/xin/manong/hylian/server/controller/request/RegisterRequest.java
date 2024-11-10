package xin.manong.hylian.server.controller.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.hylian.server.util.PasswordUtils;

import javax.ws.rs.BadRequestException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * 注册请求
 *
 * @author frankcl
 * @date 2024-10-16 16:29:29
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(RegisterRequest.class);

    /**
     * 用户名
     */
    @JsonProperty("username")
    public String username;
    /**
     * 真实名称
     */
    @JsonProperty("name")
    public String name;
    /**
     * 密码
     */
    @JsonProperty("password")
    public String password;
    /**
     * 确认密码
     */
    @JsonProperty("confirm_password")
    public String confirmPassword;
    /**
     * 验证码
     */
    @JsonProperty("captcha")
    public String captcha;

    /**
     * 检测有效性
     * 无效信息抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(username)) {
            logger.error("username is empty");
            throw new BadRequestException("用户名为空");
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("real name is empty");
            throw new BadRequestException("用户真实名称为空");
        }
        if (StringUtils.isEmpty(password)) {
            logger.error("password is empty");
            throw new BadRequestException("密码为空");
        }
        if (StringUtils.isEmpty(confirmPassword)) {
            logger.error("confirmed password is empty");
            throw new BadRequestException("确认密码为空");
        }
        if (StringUtils.isEmpty(captcha)) {
            logger.error("captcha is empty");
            throw new BadRequestException("验证码为空");
        }
        if (!password.equals(confirmPassword)) {
            logger.error("password and confirmed password are not consistent");
            throw new BadRequestException("密码与确认密码不一致");
        }
        PasswordUtils.checkPassword(password);
    }
}