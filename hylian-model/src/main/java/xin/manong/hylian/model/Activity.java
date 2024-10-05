package xin.manong.hylian.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
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
 * 活动记录：记录用户登录情况
 *
 * @author frankcl
 * @date 2023-09-01 19:38:37
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName("activity")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Activity extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger(Activity.class);

    /**
     * 自增ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Long id;
    /**
     * 会话ID
     */
    @TableField(value = "session_id")
    @JSONField(name = "session_id")
    @JsonProperty("session_id")
    public String sessionId;
    /**
     * 票据ID
     */
    @TableField(value = "ticket_id")
    @JSONField(name = "ticket_id")
    @JsonProperty("ticket_id")
    public String ticketId;
    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    @JSONField(name = "user_id")
    @JsonProperty("user_id")
    public String userId;
    /**
     * 应用ID
     */
    @TableField(value = "app_id")
    @JSONField(name = "app_id")
    @JsonProperty("app_id")
    public String appId;

    /**
     * 检测有效性，无效抛出异常BadRequestException
     */
    public void check() {
        if (StringUtils.isEmpty(sessionId)) {
            logger.error("session id is empty");
            throw new BadRequestException("session id为空");
        }
        if (StringUtils.isEmpty(ticketId)) {
            logger.error("ticket id is empty");
            throw new BadRequestException("ticket id为空");
        }
        if (StringUtils.isEmpty(userId)) {
            logger.error("user id is empty");
            throw new BadRequestException("用户ID为空");
        }
        if (StringUtils.isEmpty(appId)) {
            logger.error("app id is empty");
            throw new BadRequestException("应用ID为空");
        }
    }
}
