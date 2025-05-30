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
public class RemoveActivityRequest extends SecurityRequest {

    private static final Logger logger = LoggerFactory.getLogger(RemoveActivityRequest.class);

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
        super.check();
        if (StringUtils.isEmpty(sessionId)) {
            logger.error("Session id is empty");
            throw new BadRequestException("会话ID为空");
        }
    }
}
