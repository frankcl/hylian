package xin.manong.hylian.server.wechat;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 消息通知响应
 *
 * @author frankcl
 * @date 2025-05-29 21:45:02
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse {

    @JsonProperty("errcode")
    @JSONField(name = "errcode")
    public int code = -1;
    @JsonProperty("errmsg")
    @JSONField(name = "errmsg")
    public String message;
}
