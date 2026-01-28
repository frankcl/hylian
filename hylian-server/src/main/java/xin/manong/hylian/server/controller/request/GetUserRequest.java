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
 * 获取用户信息请求
 *
 * @author frankcl
 * @date 2026-01-12 19:09:06
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetUserRequest extends SecurityRequest {

    private static final Logger logger = LoggerFactory.getLogger(GetUserRequest.class);

    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    @JSONField(name = "user_id")
    @QueryParam("user_id")
    public String userId;

    /**
     * 检测有效性，无效抛出异常BadRequestException
     */
    public void check() {
        super.check();
        if (StringUtils.isEmpty(userId)) {
            logger.error("User id is empty");
            throw new BadRequestException("用户ID为空");
        }
    }
}
