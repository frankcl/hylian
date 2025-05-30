package xin.manong.hylian.server.controller.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 应用角色权限请求
 *
 * @author frankcl
 * @date 2023-09-05 13:51:00
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppRolePermissionsRequest extends SecurityRequest {

    private static final Logger logger = LoggerFactory.getLogger(AppRolePermissionsRequest.class);

    /**
     * 角色ID列表
     */
    @JsonProperty("role_ids")
    public List<String> roleIds;

    /**
     * 检测有效性，无效请求抛出异常
     */
    public void check() {
        super.check();
        if (roleIds == null || roleIds.isEmpty()) {
            logger.error("Role id list is empty");
            throw new BadRequestException("角色ID列表为空");
        }
    }
}
