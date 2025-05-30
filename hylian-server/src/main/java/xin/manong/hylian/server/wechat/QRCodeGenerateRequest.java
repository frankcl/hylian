package xin.manong.hylian.server.wechat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.QueryParam;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.hylian.server.service.request.SearchRequest;

/**
 * 微信小程序码生成请求
 *
 * @author frankcl
 * @date 2024-11-10 16:41:18
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QRCodeGenerateRequest extends SearchRequest {

    private static final Logger logger = LoggerFactory.getLogger(QRCodeGenerateRequest.class);

    public static final int CATEGORY_LOGIN = 1;
    public static final int CATEGORY_BIND = 2;

    /**
     * 用户ID
     */
    @JsonProperty("userid")
    @QueryParam("userid")
    public String userid;
    /**
     * 小程序码类型
     */
    @JsonProperty("category")
    @QueryParam("category")
    public Integer category;

    /**
     * 检测有效性
     * 无效信息抛出异常
     */
    public void check() {
        if (category == null) throw new BadRequestException("微信小程序码类型为空");
        if (category != CATEGORY_LOGIN && category != CATEGORY_BIND) {
            logger.error("Invalid QRCode category:{}", category);
            throw new BadRequestException("非法微信小程序码类型");
        }
        if (category == CATEGORY_BIND && StringUtils.isEmpty(userid)) {
            logger.error("Missing userid when generating bind QRCode");
            throw new BadRequestException("缺少绑定用户ID");
        }
    }
}
