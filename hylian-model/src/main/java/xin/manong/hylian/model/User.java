package xin.manong.hylian.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;

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
public class User extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger(User.class);

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
        if (StringUtils.isEmpty(userName)) {
            logger.error("user name is empty");
            throw new BadRequestException("用户名为空");
        }
        if (StringUtils.isEmpty(tenantId)) {
            logger.error("tenant id is empty");
            throw new BadRequestException("租户ID为空");
        }
        if (StringUtils.isEmpty(vendorId)) {
            logger.error("vendor id is empty");
            throw new BadRequestException("供应商ID为空");
        }
        if (StringUtils.isEmpty(password)) {
            logger.error("password is empty");
            throw new BadRequestException("密码为空");
        }
    }
}
