package xin.manong.hylian.server.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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

    @JSONField(name = "id")
    @JsonProperty("id")
    public String id;

    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    @JSONField(name = "create_time")
    @JsonProperty("create_time")
    public Long createTime;

    @JSONField(name = "update_time")
    @JsonProperty("update_time")
    public Long updateTime;
}
