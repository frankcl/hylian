package xin.manong.security.keeper.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 登录用户及应用信息
 *
 * @author frankcl
 * @date 2023-09-01 19:38:37
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("login_app")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginApp {

    /**
     * 登录用户应用ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Long id;
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
     * 应用base URL
     */
    @TableField(value = "base_url")
    @JSONField(name = "base_url")
    @JsonProperty("base_url")
    public String baseURL;
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

    public LoginApp() {
    }

    public LoginApp(String ticketId, String userId, String appId, String baseURL) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.appId = appId;
        this.baseURL = baseURL;
    }
}
