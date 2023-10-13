package xin.manong.security.keeper.server.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 角色搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 16:41:18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleSearchRequest extends SearchRequest {

    /**
     * 角色名称
     */
    @JsonProperty("name")
    public String name;
    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    public String appId;
}
