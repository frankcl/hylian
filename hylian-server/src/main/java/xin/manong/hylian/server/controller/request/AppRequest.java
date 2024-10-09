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
 * 应用信息请求
 *
 * @author frankcl
 * @date 2023-09-05 13:51:00
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(AppRequest.class);

    /**
     * 应用名
     */
    @JsonProperty("name")
    public String name;
    /**
     * 应用秘钥
     */
    @JsonProperty("secret")
    public String secret;

    /**
     * 检测有效性，无效请求抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(name)) {
            logger.error("app name is empty");
            throw new BadRequestException("应用名为空");
        }
        if (StringUtils.isEmpty(secret)) {
            logger.error("app secret is empty");
            throw new BadRequestException("应用秘钥为空");
        }
    }
}
