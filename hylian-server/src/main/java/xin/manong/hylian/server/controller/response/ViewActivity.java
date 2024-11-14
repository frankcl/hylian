package xin.manong.hylian.server.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import xin.manong.hylian.model.App;

import java.io.Serializable;

/**
 * 视图层活动记录
 *
 * @author frankcl
 * @date 2024-09-01 19:38:37
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ViewActivity implements Serializable {

    /**
     * 自增ID
     */
    @JsonProperty("id")
    public Long id;
    /**
     * 会话ID
     */
    @JsonProperty("session_id")
    public String sessionId;
    /**
     * 用户
     */
    @JsonProperty("user")
    public ViewUser user;
    /**
     * 应用
     */
    @JsonProperty("app")
    public App app;
    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    public Long createTime;
    /**
     * 更新时间
     */
    @JsonProperty("update_time")
    public Long updateTime;
}
