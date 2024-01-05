package xin.manong.security.keeper.model.view.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import xin.manong.security.keeper.model.Role;
import xin.manong.security.keeper.model.Vendor;

import java.io.Serializable;
import java.util.List;

/**
 * 视图层用户信息
 *
 * @author frankcl
 * @date 2023-09-05 10:49:24
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ViewUser implements Serializable {

    /**
     * 用户ID
     */
    @JSONField(name = "id")
    @JsonProperty("id")
    public String id;

    /**
     * 用户名
     */
    @JSONField(name = "user_name")
    @JsonProperty("user_name")
    public String userName;

    /**
     * 真实名称
     */
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    /**
     * 租户信息
     */
    @JSONField(name = "tenant")
    @JsonProperty("tenant")
    public ViewTenant tenant;

    /**
     * 供应商信息
     */
    @JSONField(name = "vendor")
    @JsonProperty("vendor")
    public Vendor vendor;

    /**
     * 角色列表
     */
    @JSONField(name = "roles")
    @JsonProperty("roles")
    public List<Role> roles;

    /**
     * 头像地址
     */
    @JSONField(name = "avatar")
    @JsonProperty("avatar")
    public String avatar;

    /**
     * 创建时间
     */
    @JSONField(name = "create_time")
    @JsonProperty("create_time")
    public Long createTime;
    /**
     * 更新时间
     */
    @JSONField(name = "update_time")
    @JsonProperty("update_time")
    public Long updateTime;
}
