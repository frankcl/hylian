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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.util.List;

/**
 * 批量用户角色请求
 *
 * @author frankcl
 * @date 2024-09-05 13:51:00
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchUserRoleRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(BatchUserRoleRequest.class);

    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    public String userId;
    /**
     * 角色ID列表
     */
    @JsonProperty("role_ids")
    public List<String> roleIds;
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
            logger.error("app id is empty");
            throw new BadRequestException("应用ID为空");
        }
        if (StringUtils.isEmpty(userId)) {
            logger.error("user id is empty");
            throw new BadRequestException("用户ID为空");
        }
    }
}
