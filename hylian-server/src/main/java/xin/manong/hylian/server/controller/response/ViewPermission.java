package xin.manong.hylian.server.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import xin.manong.hylian.model.App;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * 视图层权限
 *
 * @author frankcl
 * @date 2024-09-01 19:38:37
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ViewPermission implements Serializable {

    /**
     * 权限ID
     */
    @JsonProperty("id")
    public String id;
    /**
     * 权限名称
     */
    @JsonProperty("name")
    public String name;
    /**
     * 资源路径
     */
    @JsonProperty("resource")
    public String resource;
    /**
     * 应用
     */
    @JsonProperty("app")
    public App app;
    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    public Long createTime;
    /**
     * 更新时间
     */
    @JsonProperty("update_time")
    public Long updateTime;
}
