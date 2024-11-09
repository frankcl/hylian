package xin.manong.hylian.server.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import xin.manong.hylian.model.BaseModel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * 微信配置
 *
 * @author frankcl
 * @date 2024-11-04 11:38:22
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName("wechat")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Wechat extends BaseModel {

    /**
     * 自增ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Long id;

    /**
     * 小程序应用ID
     */
    @TableField(value = "app_id")
    @JSONField(name = "app_id")
    @JsonProperty("app_id")
    public String appId;

    /**
     * 小程序应用秘钥
     */
    @TableField(value = "app_secret")
    @JSONField(name = "app_secret")
    @JsonProperty("app_secret")
    public String appSecret;

    /**
     * 类型
     * 小程序：mini
     */
    @TableField(value = "category")
    @JSONField(name = "category")
    @JsonProperty("category")
    public String category;
}
