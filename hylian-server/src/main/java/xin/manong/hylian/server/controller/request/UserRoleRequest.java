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
 * 用户角色请求
 *
 * @author frankcl
 * @date 2023-09-05 13:51:00
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRoleRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(UserRoleRequest.class);

    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    public String userId;
    /**
     * 角色ID
     */
    @JsonProperty("role_id")
    public String roleId;
    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    public String appId;

    /**
     * 检测有效性，无效请求抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(appId)) {
            logger.error("App id is empty");
            throw new BadRequestException("应用ID为空");
        }
        if (StringUtils.isEmpty(userId)) {
            logger.error("User id is empty");
            throw new BadRequestException("用户ID为空");
        }
        if (StringUtils.isEmpty(roleId)) {
            logger.error("Role id is empty");
            throw new BadRequestException("角色ID为空");
        }
    }
}
