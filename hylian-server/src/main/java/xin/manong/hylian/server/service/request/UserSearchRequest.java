package xin.manong.hylian.server.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 用户搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 16:41:18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSearchRequest extends SearchRequest {

    /**
     * 用户名
     */
    @JsonProperty("user_name")
    public String userName;
    /**
     * 真实名称
     */
    @JsonProperty("name")
    public String name;
    /**
     * 租户ID
     */
    @JsonProperty("tenant_id")
    public String tenantId;
}
