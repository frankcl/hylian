package xin.manong.hylian.server.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.QueryParam;

/**
 * 应用搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 16:41:18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppSearchRequest extends SearchRequest {

    /**
     * 应用名
     */
    @JsonProperty("name")
    @QueryParam("name")
    public String name;

    /**
     * 应用ID
     */
    @JsonProperty("id")
    @QueryParam("id")
    public String id;
}
