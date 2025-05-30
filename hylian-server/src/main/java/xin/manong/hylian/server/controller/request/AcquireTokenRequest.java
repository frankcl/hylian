package xin.manong.hylian.server.controller.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.QueryParam;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class AcquireTokenRequest extends SecurityRequest {

    private static final Logger logger = LoggerFactory.getLogger(AcquireTokenRequest.class);

    /**
     * 安全码
     */
    @JsonProperty("code")
    @JSONField(name = "code")
    @QueryParam("code")
    public String code;
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
        super.check();
        if (StringUtils.isEmpty(code)) {
            logger.error("Code is empty");
            throw new BadRequestException("安全码为空");
        }
        if (StringUtils.isEmpty(sessionId)) {
            logger.error("App session id is empty");
            throw new BadRequestException("应用会话ID为空");
        }
    }
}
