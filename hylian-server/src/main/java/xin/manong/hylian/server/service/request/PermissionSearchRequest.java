package xin.manong.hylian.server.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.QueryParam;

/**
 * 权限搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 16:41:18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionSearchRequest extends SearchRequest {

    /**
     * 访问资源
     */
    @JsonProperty("path")
    @QueryParam("path")
    public String path;
    /**
     * 权限名称
     */
    @JsonProperty("name")
    @QueryParam("name")
    public String name;
    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    @QueryParam("app_id")
    public String appId;
}
