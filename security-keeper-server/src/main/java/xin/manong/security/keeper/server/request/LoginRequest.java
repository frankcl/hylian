package xin.manong.security.keeper.server.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
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
     * 检测有效性，无效请求抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(username)) {
            logger.error("username is empty");
            throw new BadRequestException("用户名为空");
        }
        if (StringUtils.isEmpty(password)) {
            logger.error("password is empty");
            throw new BadRequestException("密码为空");
        }
    }
}
