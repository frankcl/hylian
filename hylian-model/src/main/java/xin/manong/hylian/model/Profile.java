package xin.manong.hylian.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * 用户信息，包含用户、租户基本信息
 *
 * @author frankcl
 * @date 2023-08-31 17:26:31
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Profile {

    /**
     * 唯一ID
     */
    @JsonProperty("id")
    public String id;
    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    public String userId;
    /**
     * 租户ID
     */
    @JsonProperty("tenant_id")
    public String tenantId;
}
