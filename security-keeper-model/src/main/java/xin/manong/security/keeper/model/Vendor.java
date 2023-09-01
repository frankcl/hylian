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

/**
 * 供应商信息
 *
 * @author frankcl
 * @since 2023-08-29 17:28:08
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("vendor")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Vendor extends Model {

    private static final Logger logger = LoggerFactory.getLogger(Vendor.class);

    @TableId(value = "id")
    @JSONField(name = "id")
    @JsonProperty("id")
    public String id;

    @TableField(value = "name")
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JSONField(name = "create_time")
    @JsonProperty("create_time")
    public Long createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JSONField(name = "update_time")
    @JsonProperty("update_time")
    public Long updateTime;

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(id)) {
            logger.error("vendor id is empty");
            return false;
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("vendor name is empty");
            return false;
        }
        return true;
    }
}
