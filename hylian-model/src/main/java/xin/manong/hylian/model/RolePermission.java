package xin.manong.hylian.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
import java.util.Objects;

/**
 * 角色权限关系
 *
 * @author frankcl
 * @date 2023-03-06 15:40:19
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "role_permission", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RolePermission extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger(RolePermission.class);

    /**
     * 自增ID
     */
    @TableId(value = "id")
    @JSONField(name = "id")
    @JsonProperty("id")
    public Long id;

    /**
     * 权限ID
     */
    @TableField(value = "permission_id")
    @JSONField(name = "permission_id")
    @JsonProperty("permission_id")
    public String permissionId;

    /**
     * 角色ID
     */
    @TableField(value = "role_id")
    @JSONField(name = "role_id")
    @JsonProperty("role_id")
    public String roleId;

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RolePermission)) return false;
        RolePermission rolePermission = (RolePermission) object;
        return Objects.equals(rolePermission.permissionId, permissionId) &&
                Objects.equals(rolePermission.roleId, roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permissionId, roleId);
    }

    /**
     * 检测角色权限关系有效性
     * 无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(permissionId)) {
            logger.error("permission id is empty");
            throw new BadRequestException("权限ID为空");
        }
        if (StringUtils.isEmpty(roleId)) {
            logger.error("role id is empty");
            throw new BadRequestException("角色ID为空");
        }
    }
}
