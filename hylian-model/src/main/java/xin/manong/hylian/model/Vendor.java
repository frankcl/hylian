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
 * 供应商信息
 *
 * @author frankcl
 * @since 2023-08-29 17:28:08
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("vendor")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Vendor extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger(Vendor.class);

    @TableId(value = "id")
    @JSONField(name = "id")
    @JsonProperty("id")
    public String id;

    @TableField(value = "name")
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(id)) {
            logger.error("vendor id is empty");
            throw new BadRequestException("供应商ID为空");
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("vendor name is empty");
            throw new BadRequestException("供应商名称为空");
        }
    }
}
