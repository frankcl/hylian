package xin.manong.hylian.server.wechat;

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
import xin.manong.hylian.server.model.QRCode;

import java.io.Serializable;

/**
 * 小程序码更新请求
 *
 * @author frankcl
 * @date 2024-10-16 16:29:29
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QRCodeUpdateRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(QRCodeUpdateRequest.class);

    /**
     * 小程序码key
     */
    @JsonProperty("key")
    public String key;

    /**
     * 状态
     */
    @JsonProperty("status")
    public Integer status;

    /**
     * 检测有效性
     * 无效信息抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(key)) {
            logger.error("Key is empty");
            throw new BadRequestException("小程序码key为空");
        }
        if (status == null || status < QRCode.STATUS_ERROR || status > QRCode.STATUS_BIND) {
            logger.error("Status is invalid");
            throw new BadRequestException("小程序码状态非法");
        }
    }
}
