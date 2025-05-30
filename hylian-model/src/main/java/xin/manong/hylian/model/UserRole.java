package xin.manong.hylian.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

import java.util.Objects;

/**
 * 用户角色关系
 *
 * @author frankcl
 * @date 2023-03-06 15:40:19
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "user_role", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRole extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger(UserRole.class);

    /**
     * 自增ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Long id;

    /**
     * 用户名
     */
    @TableField(value = "user_id")
    @JSONField(name = "user_id")
    @JsonProperty("user_id")
    public String userId;

    /**
     * 角色ID
     */
    @TableField(value = "role_id")
    @JSONField(name = "role_id")
    @JsonProperty("role_id")
    public String roleId;

    /**
     * 应用ID
     */
    @TableField(value = "app_id")
    @JSONField(name = "app_id")
    @JsonProperty("app_id")
    public String appId;

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UserRole userRole)) return false;
        return Objects.equals(userRole.userId, userId) && Objects.equals(userRole.roleId, roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId);
    }

    /**
     * 检测用户角色关系有效性
     * 无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(userId)) {
            logger.error("User id is empty");
            throw new BadRequestException("用户ID为空");
        }
        if (StringUtils.isEmpty(appId)) {
            logger.error("App id is empty");
            throw new BadRequestException("应用ID为空");
        }
        if (StringUtils.isEmpty(roleId)) {
            logger.error("Role id is empty");
            throw new BadRequestException("角色ID为空");
        }
    }
}
