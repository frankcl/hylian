package xin.manong.hylian.server.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * 上传响应
 *
 * @author frankcl
 * @date 2024-09-28 21:43:38
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadResponse {

    /**
     * OSS地址
     */
    @JsonProperty("oss_url")
    public String ossURL;

    /**
     * 加签OSS地址
     */
    @JsonProperty("signed_url")
    public String signedURL;
}
