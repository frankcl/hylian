package xin.manong.hylian.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
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

/**
 * 用户信息
 *
 * @author frankcl
 * @date 2023-03-06 15:40:19
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "user", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger(User.class);

    public static final int REGISTER_MODE_NORMAL = 0;
    public static final int REGISTER_MODE_WECHAT = 1;

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
    @TableField(value = "username")
    @JSONField(name = "username")
    @JsonProperty("username")
    public String username;

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
     * 头像地址
     */
    @TableField(value = "avatar")
    @JSONField(name = "avatar")
    @JsonProperty("avatar")
    public String avatar;

    /**
     * 微信UID
     */
    @TableField(value = "wx_openid")
    @JSONField(name = "wx_openid")
    @JsonProperty("wx_openid")
    public String wxOpenid;

    /**
     * 是否禁用
     */
    @TableField(value = "disabled")
    @JSONField(name = "disabled")
    @JsonProperty("disabled")
    public Boolean disabled;

    /**
     * 注册方式
     * 普通注册：0
     * 微信注册：1
     */
    @TableField(value = "register_mode")
    @JSONField(name = "register_mode")
    @JsonProperty("register_mode")
    public Integer registerMode;

    /**
     * 是否为超级管理员
     */
    @JSONField(name = "super_admin")
    @JsonProperty("super_admin")
    @TableField(exist = false)
    public boolean superAdmin;

    /**
     * 租户信息
     */
    @JSONField(name = "tenant")
    @JsonProperty("tenant")
    @TableField(exist = false)
    public Tenant tenant;

    /**
     * 检测用户有效性
     * 无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(id)) {
            logger.error("user id is empty");
            throw new BadRequestException("用户ID为空");
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("real name is empty");
            throw new BadRequestException("用户真实名称为空");
        }
        if (StringUtils.isEmpty(username)) {
            logger.error("username is empty");
            throw new BadRequestException("用户名为空");
        }
        if (StringUtils.isEmpty(tenantId)) {
            logger.error("tenant id is empty");
            throw new BadRequestException("租户ID为空");
        }
        if (StringUtils.isEmpty(password)) {
            logger.error("password is empty");
            throw new BadRequestException("密码为空");
        }
    }
}
