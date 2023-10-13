package xin.manong.security.keeper.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.security.keeper.model.handler.JSONStringListTypeHandler;

import javax.ws.rs.BadRequestException;
import java.util.List;

/**
 * 角色定义
 *
 * @author frankcl
 * @date 2023-08-29 16:44:51
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "role", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Role extends Model {

    private static final Logger logger = LoggerFactory.getLogger(Role.class);

    /**
     * 角色ID
     */
    @TableId(value = "id")
    @JSONField(name = "id")
    @JsonProperty("id")
    public String id;

    /**
     * 角色名
     */
    @TableField(value = "name")
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    /**
     * 应用ID
     */
    @TableField(value = "app_id")
    @JSONField(name = "app_id")
    @JsonProperty("app_id")
    public String appId;

    /**
     * 权限列表
     */
    @TableField(value = "permissions", typeHandler = JSONStringListTypeHandler.class)
    @JSONField(name = "permissions")
    @JsonProperty("permissions")
    public List<String> permissions;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JSONField(name = "create_time")
    @JsonProperty("create_time")
    public Long createTime;
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JSONField(name = "update_time")
    @JsonProperty("update_time")
    public Long updateTime;

    /**
     * 检测角色有效性
     * 无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(id)) {
            logger.error("role id is empty");
            throw new BadRequestException("角色ID为空");
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("role name is empty");
            throw new BadRequestException("角色名称为空");
        }
        if (StringUtils.isEmpty(appId)) {
            logger.error("app id is empty");
            throw new BadRequestException("应用ID为空");
        }
    }
}
