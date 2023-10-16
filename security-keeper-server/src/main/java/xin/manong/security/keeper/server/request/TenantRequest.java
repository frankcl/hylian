package xin.manong.security.keeper.server.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.io.Serializable;

/**
 * 租户信息请求
 *
 * @author frankcl
 * @date 2023-09-05 13:51:00
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TenantRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(TenantRequest.class);

    /**
     * 租户名
     */
    @JsonProperty("name")
    public String name;
    /**
     * 供应商ID
     */
    @JsonProperty("vendor_id")
    public String vendorId;

    /**
     * 检测有效性，无效请求抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(name)) {
            logger.error("tenant name is empty");
            throw new BadRequestException("租户名为空");
        }
        if (StringUtils.isEmpty(vendorId)) {
            logger.error("vendor id is empty");
            throw new BadRequestException("供应商ID为空");
        }
    }
}
