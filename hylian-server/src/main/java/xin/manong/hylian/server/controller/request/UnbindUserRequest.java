package xin.manong.hylian.server.controller.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * 解绑微信账号请求
 *
 * @author frankcl
 * @date 2024-10-16 16:29:29
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnbindUserRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(UnbindUserRequest.class);

    /**
     * 用户ID
     */
    @JsonProperty("id")
    public String id;

    /**
     * 检测有效性
     * 无效信息抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(id)) {
            logger.error("user id is empty");
            throw new BadRequestException("用户ID为空");
        }
    }
}
