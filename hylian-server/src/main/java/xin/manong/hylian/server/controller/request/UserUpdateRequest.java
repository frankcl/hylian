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
 * 更新用户信息请求
 *
 * @author frankcl
 * @date 2023-10-16 16:29:29
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
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
     * 用户名
     */
    @JsonProperty("username")
    public String username;

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
        if (StringUtils.isEmpty(id)) {
            logger.error("User id is empty");
            throw new BadRequestException("用户ID为空");
        }
        if (StringUtils.isEmpty(name) && StringUtils.isEmpty(tenantId) &&
                StringUtils.isEmpty(username) && StringUtils.isEmpty(avatar) &&
                disabled == null) {
            logger.error("User info is empty");
            throw new BadRequestException("用户信息为空");
        }
    }
}
