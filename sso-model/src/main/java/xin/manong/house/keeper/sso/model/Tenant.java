package xin.manong.house.keeper.sso.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 租户信息
 *
 * @author frankcl
 * @since 2023-08-29 17:28:22
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("tenant")
public class Tenant extends Model {

    private static final Logger logger = LoggerFactory.getLogger(Tenant.class);

    @TableId(value = "tenant_id")
    @JSONField(name = "tenant_id")
    @JsonProperty("tenant_id")
    private String tenantId;

    @TableField("tenant_name")
    @JSONField(name = "tenant_name")
    @JsonProperty("tenant_name")
    private String tenantName;

    @TableField("vendor_id")
    @JSONField(name = "vendor_id")
    @JsonProperty("vendor_id")
    private String vendorId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JSONField(name = "create_time")
    @JsonProperty("create_time")
    private Long createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JSONField(name = "update_time")
    @JsonProperty("update_time")
    private Long updateTime;

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(vendorId)) {
            logger.error("vendor id is empty");
            return false;
        }
        if (StringUtils.isEmpty(tenantId)) {
            logger.error("tenant id is empty");
            return false;
        }
        if (StringUtils.isEmpty(tenantName)) {
            logger.error("tenant name is empty");
            return false;
        }
        return true;
    }
}
