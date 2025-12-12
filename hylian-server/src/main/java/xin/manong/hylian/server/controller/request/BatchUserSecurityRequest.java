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

import java.util.List;

/**
 * 批量获取用户信息安全请求
 *
 * @author frankcl
 * @date 2025-12-12 10:59:14
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchUserSecurityRequest extends SecurityRequest {

    /**
     * 用户ID列表
     */
    @JsonProperty("ids")
    @JSONField(name = "ids")
    @QueryParam("ids")
    public List<String> ids;

    /**
     * 检测有效性，无效请求抛出异常
     */
    public void check() {
        super.check();
        if (ids == null || ids.isEmpty()) throw new BadRequestException("用户ID列表为空");
    }
}
