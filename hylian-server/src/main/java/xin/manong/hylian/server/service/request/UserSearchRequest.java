package xin.manong.hylian.server.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.QueryParam;
import java.util.List;

/**
 * 用户搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 16:41:18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSearchRequest extends SearchRequest {

    public List<String> idList;
    /**
     * 用户ID列表
     */
    @JsonProperty("ids")
    @QueryParam("ids")
    public String ids;
    /**
     * 用户名
     */
    @JsonProperty("username")
    @QueryParam("username")
    public String username;
    /**
     * 真实名称
     */
    @JsonProperty("name")
    @QueryParam("name")
    public String name;
    /**
     * 租户ID
     */
    @JsonProperty("tenant_id")
    @QueryParam("tenant_id")
    public String tenantId;
    /**
     * 是否禁用
     */
    @JsonProperty("disabled")
    @QueryParam("disabled")
    public Boolean disabled;
}
