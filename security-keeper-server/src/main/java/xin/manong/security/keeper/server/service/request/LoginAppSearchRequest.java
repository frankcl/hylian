package xin.manong.security.keeper.server.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 登录应用搜索请求
 *
 * @author frankcl
 * @date 2023-09-05 14:16:42
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginAppSearchRequest extends SearchRequest {

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
