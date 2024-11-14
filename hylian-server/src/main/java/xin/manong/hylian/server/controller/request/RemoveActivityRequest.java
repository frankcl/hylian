package xin.manong.hylian.server.controller.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * 移除活动记录请求
 *
 * @author frankcl
 * @date 2023-09-14 11:42:04
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RemoveActivityRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(RemoveActivityRequest.class);

    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    @JSONField(name = "app_id")
    public String appId;
    /**
     * 应用秘钥
     */
    @JsonProperty("app_secret")
    @JSONField(name = "app_secret")
    public String appSecret;
    /**
     * 会话ID
     */
    @JsonProperty("session_id")
    @JSONField(name = "session_id")
    public String sessionId;

    /**
     * 检测请求有效性，无效请求抛出异常BadRequestException
     */
    public void check() {
        if (StringUtils.isEmpty(appId)) {
            logger.error("app id is empty");
            throw new BadRequestException("应用ID为空");
        }
        if (StringUtils.isEmpty(appSecret)) {
            logger.error("app secret is empty");
            throw new BadRequestException("应用秘钥为空");
        }
        if (StringUtils.isEmpty(sessionId)) {
            logger.error("session id is empty");
            throw new BadRequestException("会话ID为空");
        }
    }
}
