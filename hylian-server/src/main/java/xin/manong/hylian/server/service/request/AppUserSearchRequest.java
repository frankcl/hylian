package xin.manong.hylian.server.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 应用用户关系搜索请求
 *
 * @author frankcl
 * @date 2024-10-10 11:31:28
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppUserSearchRequest extends SearchRequest {

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
