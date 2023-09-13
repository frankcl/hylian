package xin.manong.security.keeper.model;

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

/**
 * 应用登录信息
 *
 * @author frankcl
 * @date 2023-09-01 19:38:37
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("app_login")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppLogin {

    private static final Logger logger = LoggerFactory.getLogger(AppLogin.class);

    /**
     * 自增ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Long id;
    /**
     * 应用session ID
     */
    @TableField(value = "session_id")
    @JSONField(name = "session_id")
    @JsonProperty("session_id")
    public String sessionId;
    /**
     * 登录ticket ID
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
     * 应用注销URL
     */
    @TableField(value = "logout_url")
    @JSONField(name = "logout_url")
    @JsonProperty("logout_url")
    public String logoutURL;
    /**
     * 创建时间，毫秒时间戳
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JSONField(name = "create_time")
    @JsonProperty("create_time")
    public Long createTime;
    /**
     * 更新时间，毫秒时间戳
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JSONField(name = "update_time")
    @JsonProperty("update_time")
    public Long updateTime;

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
        if (StringUtils.isEmpty(logoutURL)) {
            logger.error("logout url is empty");
            throw new BadRequestException("注销URL为空");
        }
    }
}
