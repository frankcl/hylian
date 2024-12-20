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
 * 刷新token请求
 *
 * @author frankcl
 * @date 2023-08-31 15:57:07
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefreshTokenRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenRequest.class);

    @JsonProperty("token")
    @JSONField(name = "token")
    public String token;
    @JsonProperty("app_id")
    @JSONField(name = "app_id")
    public String appId;
    @JsonProperty("app_secret")
    @JSONField(name = "app_secret")
    public String appSecret;

    /**
     * 检测有效性，无效请求抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(token)) {
            logger.error("refresh token is empty");
            throw new BadRequestException("刷新token为空");
        }
        if (StringUtils.isEmpty(appId)) {
            logger.error("app id is empty");
            throw new BadRequestException("应用ID为空");
        }
        if (StringUtils.isEmpty(appSecret)) {
            logger.error("app secret is empty");
            throw new BadRequestException("应用秘钥为空");
        }
    }
}
