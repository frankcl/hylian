package xin.manong.hylian.server.service.request;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 搜索请求
 *
 * @author frankcl
 * @date 2023-04-24 11:07:03
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchRequest implements Serializable {

    /**
     * 页码，从1开始
     */
    @JsonProperty("current")
    public Integer current;
    /**
     * 分页大小，默认20
     */
    @JsonProperty("size")
    public Integer size;
    /**
     * 排序方式
     */
    @JsonProperty("order_by")
    public List<OrderByRequest> orderBy;

    /**
     * 构建排序条件
     *
     * @param query 查询
     */
    public void prepareOrderBy(QueryWrapper<?> query) {
        if (orderBy == null || orderBy.isEmpty()) {
            query.orderBy(true, false, "create_time");
            return;
        }
        for (OrderByRequest orderByItem : orderBy) {
            query.orderBy(true, orderByItem.asc != null && orderByItem.asc, orderByItem.field);
        }
    }
}
