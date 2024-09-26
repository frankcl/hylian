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
import java.io.Serializable;

/**
 * 用户信息请求
 *
 * @author frankcl
 * @date 2023-10-16 16:29:29
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(UserRequest.class);

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
     * 确认密码
     */
    @JsonProperty("confirmed_password")
    public String confirmedPassword;

    /**
     * 真实名称
     */
    @JsonProperty("name")
    public String name;

    /**
     * 租户ID
     */
    @JsonProperty("tenant_id")
    public String tenantId;

    /**
     * 头像地址
     */
    @JsonProperty("avatar")
    public String avatar;

    /**
     * 检测有效性
     * 无效信息抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(userName)) {
            logger.error("user name is empty");
            throw new BadRequestException("用户名为空");
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("real name is empty");
            throw new BadRequestException("用户真实名称为空");
        }
        if (StringUtils.isEmpty(tenantId)) {
            logger.error("tenant id is empty");
            throw new BadRequestException("租户ID为空");
        }
        if (StringUtils.isEmpty(password)) {
            logger.error("password is empty");
            throw new BadRequestException("密码为空");
        }
        if (StringUtils.isEmpty(confirmedPassword)) {
            logger.error("confirmed password is empty");
            throw new BadRequestException("确认密码为空");
        }
        if (!password.equals(confirmedPassword)) {
            logger.error("password and confirmed password are not consistent");
            throw new BadRequestException("密码与确认密码不一致");
        }
        PasswordUtils.checkPassword(password);
    }
}
