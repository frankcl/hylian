package xin.manong.security.keeper.server.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import xin.manong.security.keeper.model.Vendor;

import java.io.Serializable;

/**
 * 视图层租户信息
 *
 * @author frankcl
 * @date 2023-09-05 10:51:30
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ViewTenant implements Serializable {

    @TableId(value = "id")
    @JSONField(name = "id")
    @JsonProperty("id")
    public String id;

    @TableField("name")
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    @TableField("vendor")
    @JSONField(name = "vendor")
    @JsonProperty("vendor")
    public Vendor vendor;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JSONField(name = "create_time")
    @JsonProperty("create_time")
    public Long createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JSONField(name = "update_time")
    @JsonProperty("update_time")
    public Long updateTime;
}
