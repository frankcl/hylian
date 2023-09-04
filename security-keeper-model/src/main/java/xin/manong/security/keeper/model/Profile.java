package xin.manong.security.keeper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 用户简介
 *
 * @author frankcl
 * @date 2023-08-31 17:26:31
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Profile {

    @JsonProperty("id")
    public String id;
    @JsonProperty("user_id")
    public String userId;
    @JsonProperty("tenant_id")
    public String tenantId;
    @JsonProperty("vendor_id")
    public String vendorId;

    public Profile() {
    }

    public Profile(String id, String userId, String tenantId, String vendorId) {
        this.id = id;
        this.userId = userId;
        this.tenantId = tenantId;
        this.vendorId = vendorId;
    }
}
