package xin.manong.hylian.server.controller.request;

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
 * 批量角色权限请求
 *
 * @author frankcl
 * @date 2024-09-05 13:51:00
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchRolePermissionRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(BatchRolePermissionRequest.class);

    /**
     * 权限ID列表
     */
    @JsonProperty("permission_ids")
    public List<String> permissionIds;
    /**
     * 角色ID
     */
    @JsonProperty("role_id")
    public String roleId;

    /**
     * 检测有效性，无效请求抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(roleId)) {
            logger.error("role id is empty");
            throw new BadRequestException("角色ID为空");
        }
    }
}
