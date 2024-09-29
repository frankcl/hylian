package xin.manong.hylian.server.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.hylian.common.util.PasswordUtils;

import javax.ws.rs.BadRequestException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * 更新密码请求
 *
 * @author frankcl
 * @date 2023-09-05 11:18:03
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PasswordChangeRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(PasswordChangeRequest.class);

    /**
     * 用户名
     */
    @JsonProperty("user_name")
    public String userName;
    /**
     * 密码
     */
    @JsonProperty("password")
    public String password;
    /**
     * 新密码
     */
    @JsonProperty("new_password")
    public String newPassword;
    /**
     * 确认新密码
     */
    @JsonProperty("confirm_password")
    public String confirmPassword;

    /**
     * 检测有效性，无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(userName)) {
            logger.error("user name is empty");
            throw new BadRequestException("用户名为空");
        }
        if (StringUtils.isEmpty(password)) {
            logger.error("password is empty");
            throw new BadRequestException("密码为空");
        }
        if (StringUtils.isEmpty(newPassword)) {
            logger.error("new password is empty");
            throw new BadRequestException("新密码为空");
        }
        if (StringUtils.isEmpty(confirmPassword)) {
            logger.error("confirm new password is empty");
            throw new BadRequestException("确认新密码为空");
        }
        if (!newPassword.equals(confirmPassword)) {
            logger.error("new password and confirmed new password are not consistent");
            throw new BadRequestException("新密码与确认新密码不一致");
        }
        PasswordUtils.checkPassword(newPassword);
    }
}
