package xin.manong.house.keeper.sso.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.house.keeper.sso.model.handler.JSONRoleListTypeHandler;

import java.util.List;

/**
 * 用户信息
 *
 * @author frankcl
 * @date 2023-03-06 15:40:19
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "user", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User extends Model {

    private static final Logger logger = LoggerFactory.getLogger(User.class);

    /**
     * 用户ID
     */
    @TableId(value = "user_id")
    @JSONField(name = "user_id")
    @JsonProperty("user_id")
    public String userId;

    /**
     * 用户名
     */
    @TableField(value = "user_name")
    @JSONField(name = "user_name")
    @JsonProperty("user_name")
    public String userName;

    /**
     * 密码
     */
    @TableField(value = "password")
    @JSONField(name = "password")
    @JsonProperty("password")
    public String password;

    /**
     * 真实名称
     */
    @TableField(value = "name")
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id")
    @JSONField(name = "tenant_id")
    @JsonProperty("tenant_id")
    public String tenantId;

    /**
     * ISV ID
     */
    @TableField(value = "isv_id")
    @JSONField(name = "isv_id")
    @JsonProperty("isv_id")
    public String isvId;

    /**
     * 头像地址
     */
    @TableField(value = "avatar")
    @JSONField(name = "avatar")
    @JsonProperty("avatar")
    public String avatar;

    /**
     * 角色列表
     */
    @TableField(value = "roles", typeHandler = JSONRoleListTypeHandler.class)
    @JSONField(name = "roles")
    @JsonProperty("roles")
    public List<Role> roles;

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
     * 检测用户有效性
     *
     * @return 如果有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(userId)) {
            logger.error("user id is empty");
            return false;
        }
        if (StringUtils.isEmpty(userName)) {
            logger.error("user name is empty");
            return false;
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("real name is empty");
            return false;
        }
        if (StringUtils.isEmpty(password)) {
            logger.error("password is empty");
            return false;
        }
        if (StringUtils.isEmpty(tenantId)) {
            logger.error("tenant id is empty");
            return false;
        }
        if (StringUtils.isEmpty(isvId)) {
            logger.error("isv id is empty");
            return false;
        }
        return true;
    }
}
