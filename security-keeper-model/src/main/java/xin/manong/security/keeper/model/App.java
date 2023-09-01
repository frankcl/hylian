package xin.manong.security.keeper.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 应用信息
 *
 * @author frankcl
 * @since 2023-08-30 10:25:08
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("app")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class App extends Model {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    @TableId(value = "id")
    @JSONField(name = "id")
    @JsonProperty("id")
    public String id;

    @TableField("`name`")
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    @TableField("secret")
    @JSONField(name = "secret")
    @JsonProperty("secret")
    public String secret;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JSONField(name = "create_time")
    @JsonProperty("create_time")
    public Long createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JSONField(name = "update_time")
    @JsonProperty("update_time")
    public Long updateTime;

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(id)) {
            logger.error("app id is empty");
            return false;
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("app name is empty");
            return false;
        }
        if (StringUtils.isEmpty(secret)) {
            logger.error("app secret is empty");
            return false;
        }
        return true;
    }
}
