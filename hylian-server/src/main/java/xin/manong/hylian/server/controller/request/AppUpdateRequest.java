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

/**
 * 应用更新请求
 *
 * @author frankcl
 * @date 2023-10-16 15:13:50
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppUpdateRequest extends AppRequest {

    private static final Logger logger = LoggerFactory.getLogger(AppUpdateRequest.class);

    /**
     * 应用ID
     */
    @JsonProperty("id")
    public String id;

    /**
     * 检测有效性，无效请求抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(id)) {
            logger.error("app id is empty");
            throw new BadRequestException("应用ID为空");
        }
        if (StringUtils.isEmpty(name) && StringUtils.isEmpty(secret)) {
            logger.error("update app info is empty");
            throw new BadRequestException("修改应用信息为空");
        }
    }
}
