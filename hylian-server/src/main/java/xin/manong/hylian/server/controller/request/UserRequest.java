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
import xin.manong.hylian.server.util.PasswordUtils;

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
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(UserRequest.class);

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
     * 确认密码
     */
    @JsonProperty("confirm_password")
    public String confirmPassword;

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
     * 是否禁用
     */
    @JsonProperty("disabled")
    public Boolean disabled;

    /**
     * 检测有效性
     * 无效信息抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(username)) {
            logger.error("Username is empty");
            throw new BadRequestException("用户名为空");
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("Nickname is empty");
            throw new BadRequestException("用户昵称为空");
        }
        if (StringUtils.isEmpty(tenantId)) {
            logger.error("Tenant id is empty");
            throw new BadRequestException("租户ID为空");
        }
        if (StringUtils.isEmpty(password)) {
            logger.error("Password is empty");
            throw new BadRequestException("密码为空");
        }
        if (StringUtils.isEmpty(confirmPassword)) {
            logger.error("Confirmed password is empty");
            throw new BadRequestException("确认密码为空");
        }
        if (!password.equals(confirmPassword)) {
            logger.error("Password and confirmed password are not consistent");
            throw new BadRequestException("密码与确认密码不一致");
        }
        PasswordUtils.checkPassword(password);
    }
}
