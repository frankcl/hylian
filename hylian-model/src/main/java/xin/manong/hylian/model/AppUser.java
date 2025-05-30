package xin.manong.hylian.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 应用用户关系
 *
 * @author frankcl
 * @since 2024-10-10 10:25:08
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName("app_user")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppUser extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger(AppUser.class);

    /**
     * 自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Long id;

    /**
     * 应用ID
     */
    @TableField("app_id")
    @JSONField(name = "app_id")
    @JsonProperty("app_id")
    public String appId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @JSONField(name = "user_id")
    @JsonProperty("user_id")
    public String userId;

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AppUser)) return false;
        AppUser appUser = (AppUser) object;
        return Objects.equals(appUser.appId, appId) &&
                Objects.equals(appUser.userId, userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, userId);
    }

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(appId)) {
            logger.error("App id is empty");
            throw new BadRequestException("应用ID为空");
        }
        if (StringUtils.isEmpty(userId)) {
            logger.error("User id is empty");
            throw new BadRequestException("用户ID为空");
        }
    }
}
