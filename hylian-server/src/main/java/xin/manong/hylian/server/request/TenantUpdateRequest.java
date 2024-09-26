package xin.manong.hylian.server.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;

/**
 * 租户更新请求
 *
 * @author frankcl
 * @date 2023-09-05 13:51:00
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TenantUpdateRequest extends TenantRequest {

    private static final Logger logger = LoggerFactory.getLogger(TenantUpdateRequest.class);

    /**
     * 租户ID
     */
    @JsonProperty("id")
    public String id;

    /**
     * 检测有效性，无效请求抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(id)) {
            logger.error("tenant id is empty");
            throw new BadRequestException("租户ID为空");
        }
        if (StringUtils.isEmpty(name) && StringUtils.isEmpty(vendorId)) {
            logger.error("update tenant info is empty");
            throw new BadRequestException("更新租户信息为空");
        }
    }
}
