package xin.manong.hylian.server.request;

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
 * 更新用户信息请求
 *
 * @author frankcl
 * @date 2023-10-16 16:29:29
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserUpdateRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(UserUpdateRequest.class);

    /**
     * 用户ID
     */
    @JsonProperty("id")
    public String id;

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
        if (StringUtils.isEmpty(id)) {
            logger.error("user id is empty");
            throw new BadRequestException("用户ID为空");
        }
        if (StringUtils.isEmpty(name) && StringUtils.isEmpty(tenantId) && StringUtils.isEmpty(avatar)) {
            logger.error("user info is empty");
            throw new BadRequestException("用户信息为空");
        }
    }
}
