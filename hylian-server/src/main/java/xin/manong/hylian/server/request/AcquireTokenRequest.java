package xin.manong.hylian.server.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.QueryParam;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * 获取token请求
 *
 * @author frankcl
 * @date 2023-09-12 19:09:06
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AcquireTokenRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(AcquireTokenRequest.class);

    /**
     * 安全码
     */
    @JsonProperty("code")
    @JSONField(name = "code")
    @QueryParam("code")
    public String code;
    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    @JSONField(name = "app_id")
    @QueryParam("app_id")
    public String appId;
    /**
     * 应用秘钥
     */
    @JsonProperty("app_secret")
    @JSONField(name = "app_secret")
    @QueryParam("app_secret")
    public String appSecret;
    /**
     * 应用会话ID
     */
    @JsonProperty("session_id")
    @JSONField(name = "session_id")
    @QueryParam("session_id")
    public String sessionId;

    /**
     * 检测有效性，无效抛出异常BadRequestException
     */
    public void check() {
        if (StringUtils.isEmpty(code)) {
            logger.error("code is empty");
            throw new BadRequestException("安全码为空");
        }
        if (StringUtils.isEmpty(appId)) {
            logger.error("app id is empty");
            throw new BadRequestException("应用ID为空");
        }
        if (StringUtils.isEmpty(appSecret)) {
            logger.error("app secret is empty");
            throw new BadRequestException("应用秘钥为空");
        }
        if (StringUtils.isEmpty(sessionId)) {
            logger.error("app session id is empty");
            throw new BadRequestException("应用会话ID为空");
        }
    }
}
