package xin.manong.hylian.server.controller.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.hylian.client.util.PermissionUtils;

import javax.ws.rs.BadRequestException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * 权限信息请求
 *
 * @author frankcl
 * @date 2023-09-05 13:51:00
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(PermissionRequest.class);

    /**
     * 权限名
     */
    @JsonProperty("name")
    public String name;

    /**
     * 访问资源
     */
    @JsonProperty("path")
    public String path;

    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    public String appId;

    /**
     * 检测有效性，无效请求抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(name)) {
            logger.error("permission name is empty");
            throw new BadRequestException("权限名为空");
        }
        if (StringUtils.isEmpty(path)) {
            logger.error("permission resource is empty");
            throw new BadRequestException("权限资源为空");
        }
        if (StringUtils.isEmpty(appId)) {
            logger.error("app id is empty");
            throw new BadRequestException("应用ID为空");
        }
        PermissionUtils.validate(path);
    }
}
