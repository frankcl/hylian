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
import java.util.List;

/**
 * 应用角色权限请求
 *
 * @author frankcl
 * @date 2023-09-05 13:51:00
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppRolePermissionsRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(AppRolePermissionsRequest.class);

    private static final int DEFAULT_SIZE = 100;

    /**
     * 角色ID列表
     */
    @JsonProperty("role_ids")
    public List<String> roleIds;

    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    public String appId;

    /**
     * 应用秘钥
     */
    @JsonProperty("app_secret")
    public String appSecret;

    /**
     * 获取数量
     */
    @JsonProperty("size")
    public Integer size;

    /**
     * 检测有效性，无效请求抛出异常
     */
    public void check() {
        if (roleIds == null || roleIds.isEmpty()) {
            logger.error("role id list is empty");
            throw new BadRequestException("角色ID列表为空");
        }
        if (StringUtils.isEmpty(appId)) {
            logger.error("app id is empty");
            throw new BadRequestException("应用ID为空");
        }
        if (StringUtils.isEmpty(appSecret)) {
            logger.error("app secret is empty");
            throw new BadRequestException("应用秘钥为空");
        }
        if (size == null || size <= 0) size = DEFAULT_SIZE;
    }
}
