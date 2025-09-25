package xin.manong.hylian.server.wechat;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 微信电话响应
 *
 * @author frankcl
 * @date 2024-11-04 11:15:43
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhoneResponse {

    /**
     * 错误码
     */
    @JsonProperty("errcode")
    @JSONField(name = "errcode")
    public int code = -1;
    /**
     * 错误信息
     */
    @JsonProperty("errmsg")
    @JSONField(name = "errmsg")
    public String message;
    /**
     * 电话信息
     */
    @JsonProperty("phone_info")
    @JSONField(name = "phone_info")
    public PhoneInfo phoneInfo;
}
