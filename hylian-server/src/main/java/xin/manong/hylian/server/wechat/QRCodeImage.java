package xin.manong.hylian.server.wechat;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * 小程序码图片
 *
 * @author frankcl
 * @date 2024-10-16 16:29:29
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QRCodeImage implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(QRCodeImage.class);

    /**
     * 小程序码key
     */
    @JsonProperty("key")
    @JSONField(name = "key")
    public String key;

    /**
     * 图片base64
     */
    @JsonProperty("image")
    @JSONField(name = "image")
    public String image;
}
