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

/**
 * 权限更新请求
 *
 * @author frankcl
 * @date 2023-10-16 15:34:42
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionUpdateRequest extends PermissionRequest {

    private static final Logger logger = LoggerFactory.getLogger(PermissionUpdateRequest.class);

    /**
     * 权限ID
     */
    @JsonProperty("id")
    public String id;

    /**
     * 检测有效性，无效请求抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(id)) {
            logger.error("permission id is empty");
            throw new BadRequestException("权限ID为空");
        }
        if (StringUtils.isEmpty(name) && StringUtils.isEmpty(path) && StringUtils.isEmpty(appId)) {
            logger.error("update permission info is empty");
            throw new BadRequestException("更新权限信息为空");
        }
        if (path != null) PermissionUtils.validate(path);
    }
}
