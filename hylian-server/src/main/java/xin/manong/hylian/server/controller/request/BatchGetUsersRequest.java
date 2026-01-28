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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 批量获取用户信息请求
 *
 * @author frankcl
 * @date 2026-01-12 19:09:06
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchGetUsersRequest extends SecurityRequest {

    private static final Logger logger = LoggerFactory.getLogger(BatchGetUsersRequest.class);

    /**
     * 用户ID列表
     */
    @JsonProperty("user_ids")
    @JSONField(name = "user_ids")
    @QueryParam("user_ids")
    public List<String> userIds;

    /**
     * 检测有效性，无效抛出异常BadRequestException
     */
    public void check() {
        super.check();
        if (userIds == null || userIds.isEmpty()) {
            logger.error("User ids are empty");
            throw new BadRequestException("用户ID列表为空");
        }
    }
}
