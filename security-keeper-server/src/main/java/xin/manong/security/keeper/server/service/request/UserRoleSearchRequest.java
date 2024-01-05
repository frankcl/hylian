package xin.manong.security.keeper.server.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import xin.manong.security.keeper.model.view.request.SearchRequest;

/**
 * 用户角色关系搜索请求
 *
 * @author frankcl
 * @date 2023-10-16 11:31:28
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRoleSearchRequest extends SearchRequest {

    /**
     * 角色ID
     */
    @JsonProperty("role_id")
    public String roleId;
    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    public String userId;
    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    public String appId;
}
