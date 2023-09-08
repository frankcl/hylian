package xin.manong.security.keeper.model;

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
import xin.manong.security.keeper.model.handler.JSONRoleListTypeHandler;

import javax.ws.rs.BadRequestException;
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

    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * 用户ID
     */
    @TableId(value = "id")
    @JSONField(name = "id")
    @JsonProperty("id")
    public String id;

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
     * 供应商ID
     */
    @TableField(value = "vendor_id")
    @JSONField(name = "vendor_id")
    @JsonProperty("vendor_id")
    public String vendorId;

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
     * 无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(id)) {
            logger.error("user id is empty");
            throw new BadRequestException("用户ID为空");
        }
        if (StringUtils.isEmpty(userName)) {
            logger.error("user name is empty");
            throw new BadRequestException("用户名为空");
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("real name is empty");
            throw new BadRequestException("用户真实名称为空");
        }
        if (StringUtils.isEmpty(tenantId)) {
            logger.error("tenant id is empty");
            throw new BadRequestException("租户ID为空");
        }
        if (StringUtils.isEmpty(vendorId)) {
            logger.error("vendor id is empty");
            throw new BadRequestException("供应商ID为空");
        }
        checkPassword(password);
    }

    /**
     * 检查密码
     *
     * @param password 密码
     */
    public void checkPassword(String password) {
        password = password == null ? "" : password.trim();
        if (StringUtils.isEmpty(password) || password.length() < MIN_PASSWORD_LENGTH) {
            logger.error("password length[{}] is invalid", password == null ? 0 : password.length());
            throw new BadRequestException(String.format("秘钥最小长度[%d]", password.length()));
        }
        int lowerCaseLetters = 0, upperCaseLetters = 0, digits = 0, others = 0;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (c >= 'A' && c <= 'Z') upperCaseLetters++;
            else if (c >= 'a' && c <= 'z') lowerCaseLetters++;
            else if (c >= '0' && c <= '9') digits++;
            else others++;
        }
        if (lowerCaseLetters == 0 || upperCaseLetters == 0 || digits == 0 || others == 0) {
            logger.error("password security is weak");
            throw new BadRequestException("密码安全性弱，建议包含大小写字母、数字及特殊符号");
        }
    }
}
