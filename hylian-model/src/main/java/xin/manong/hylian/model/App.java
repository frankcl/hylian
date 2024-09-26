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
public class App extends BaseModel {

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

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(id)) {
            logger.error("app id is empty");
            throw new BadRequestException("应用ID为空");
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("app name is empty");
            throw new BadRequestException("应用名为空");
        }
        if (StringUtils.isEmpty(secret)) {
            logger.error("app secret is empty");
            throw new BadRequestException("应用秘钥为空");
        }
    }
}
