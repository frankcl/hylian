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
import javax.ws.rs.FormParam;
import java.io.Serializable;

/**
 * 登录请求对象
 *
 * @author frankcl
 * @date 2023-08-31 19:49:08
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
    @JsonProperty("user_name")
    @FormParam("user_name")
    public String userName;
    /**
     * 密码
     */
    @JsonProperty("password")
    @FormParam("password")
    public String password;
    /**
     * 重定向URL
     */
    @JsonProperty("redirect_url")
    @FormParam("redirect_url")
    public String redirectURL;

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
        if (StringUtils.isEmpty(redirectURL)) {
            logger.error("redirect url is empty");
            throw new BadRequestException("重定向URL为空");
        }
    }
}
