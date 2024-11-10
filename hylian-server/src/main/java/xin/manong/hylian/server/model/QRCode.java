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
 * 微信小程序码
 *
 * @author frankcl
 * @date 2024-11-04 22:30:25
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName("qr_code")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QRCode extends BaseModel {

    public static final int STATUS_ERROR = -1;
    public static final int STATUS_WAIT = 0;
    public static final int STATUS_SCANNED = 1;
    public static final int STATUS_AUTHORIZED = 2;
    public static final int STATUS_BIND = 3;

    /**
     * 自增ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Long id;

    /**
     * 小程序码唯一key
     */
    @TableField(value = "`key`")
    @JSONField(name = "key")
    @JsonProperty("key")
    public String key;

    /**
     * 小程序用户ID
     */
    @TableField(value = "openid")
    @JSONField(name = "openid")
    @JsonProperty("openid")
    public String openid;

    /**
     * 用户ID
     */
    @TableField(value = "userid")
    @JSONField(name = "userid")
    @JsonProperty("userid")
    public String userid;

    /**
     * 小程序码状态
     * -1：错误
     * 0：等待扫码
     * 1：已扫码
     * 2：已授权
     */
    @TableField(value = "status")
    @JSONField(name = "status")
    @JsonProperty("status")
    public Integer status;

    /**
     * 错误信息
     */
    @TableField(exist = false)
    @JSONField(name = "message")
    @JsonProperty("message")
    public String message;
}
