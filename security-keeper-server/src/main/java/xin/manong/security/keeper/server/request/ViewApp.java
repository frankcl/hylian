package xin.manong.security.keeper.server.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 视图层应用信息
 *
 * @author frankcl
 * @date 2023-09-05 13:51:00
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ViewApp {

    /**
     * 应用ID
     */
    @JsonProperty("id")
    public String id;
    /**
     * 应用名
     */
    @JsonProperty("name")
    public String name;
}
