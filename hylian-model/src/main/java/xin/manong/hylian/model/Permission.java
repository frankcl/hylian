package xin.manong.hylian.model;

import com.alibaba.fastjson.annotation.JSONField;
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

/**
 * 权限定义
 *
 * @author frankcl
 * @date 2023-10-13 11:44:49
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "permission", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Permission extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger(Permission.class);

    /**
     * 权限ID
     */
    @TableId(value = "id")
    @JSONField(name = "id")
    @JsonProperty("id")
    public String id;

    /**
     * 权限名
     */
    @TableField(value = "name")
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    /**
     * 访问资源
     */
    @TableField(value = "path")
    @JSONField(name = "path")
    @JsonProperty("path")
    public String path;

    /**
     * 应用ID
     */
    @TableField(value = "app_id")
    @JSONField(name = "app_id")
    @JsonProperty("app_id")
    public String appId;

    /**
     * 检测权限有效性
     * 无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(id)) {
            logger.error("Permission id is empty");
            throw new BadRequestException("权限ID为空");
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("Permission name is empty");
            throw new BadRequestException("权限名称为空");
        }
        if (StringUtils.isEmpty(path)) {
            logger.error("Path is empty");
            throw new BadRequestException("资源路径为空");
        }
        if (StringUtils.isEmpty(appId)) {
            logger.error("App id is empty");
            throw new BadRequestException("应用ID为空");
        }
    }
}
