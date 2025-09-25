package xin.manong.hylian.server.wechat;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 微信电话信息
 *
 * @author frankcl
 * @date 2024-11-04 11:15:43
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhoneInfo {

    /**
     * 电话号码
     */
    @JsonProperty("phoneNumber")
    @JSONField(name = "phoneNumber")
    public String phoneNumber;
    /**
     * 不带区号电话号码
     */
    @JsonProperty("purePhoneNumber")
    @JSONField(name = "purePhoneNumber")
    public String purePhoneNumber;
    /**
     * 区号
     */
    @JsonProperty("countryCode")
    @JSONField(name = "countryCode")
    public String countryCode;
}
