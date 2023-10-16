package xin.manong.security.keeper.server.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 角色权限关系搜索请求
 *
 * @author frankcl
 * @date 2023-10-16 11:31:28
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RolePermissionSearchRequest extends SearchRequest {

    /**
     * 角色ID
     */
    @JsonProperty("role_id")
    public String roleId;
    /**
     * 权限ID
     */
    @JsonProperty("permission_id")
    public String permissionId;
}
