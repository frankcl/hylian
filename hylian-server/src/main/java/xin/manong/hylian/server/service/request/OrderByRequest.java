package xin.manong.hylian.server.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 排序请求
 *
 * @author frankcl
 * @date 2024-10-02 17:07:31
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderByRequest implements Serializable {

    /**
     * 排序字段
     */
    @JsonProperty("field")
    public String field;
    /**
     * 排序方式
     * true：升序
     * false：降序
     */
    @JsonProperty("asc")
    public Boolean asc = true;
}
