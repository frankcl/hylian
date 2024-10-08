package xin.manong.hylian.server.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import xin.manong.hylian.model.Role;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.util.List;

/**
 * 视图层用户信息
 *
 * @author frankcl
 * @date 2024-09-05 10:49:24
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ViewUser implements Serializable {

    /**
     * 用户ID
     */
    @JsonProperty("id")
    public String id;

    /**
     * 用户名
     */
    @JsonProperty("username")
    public String username;

    /**
     * 真实名称
     */
    @JsonProperty("name")
    public String name;

    /**
     * 租户信息
     */
    @JsonProperty("tenant")
    public ViewTenant tenant;

    /**
     * 角色列表
     */
    @JsonProperty("roles")
    public List<Role> roles;

    /**
     * 头像地址
     */
    @JsonProperty("avatar")
    public String avatar;

    /**
     * 是否禁用
     */
    @JsonProperty("disabled")
    public boolean disabled = true;

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
