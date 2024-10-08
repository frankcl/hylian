package xin.manong.hylian.server.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.QueryParam;

/**
 * 活动记录搜索请求
 *
 * @author frankcl
 * @date 2023-09-05 14:16:42
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivitySearchRequest extends SearchRequest {

    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    @QueryParam("user_id")
    public String userId;

    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    @QueryParam("app_id")
    public String appId;

    /**
     * 会话ID
     */
    @JsonProperty("session_id")
    @QueryParam("session_id")
    public String sessionId;
}
