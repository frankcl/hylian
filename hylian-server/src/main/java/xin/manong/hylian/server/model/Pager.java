package xin.manong.hylian.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页对象
 *
 * @author frankcl
 * @date 2022-09-21 11:26:50
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Pager<T> implements Serializable {

    /**
     * 页码：从1开始
     */
    @JsonProperty("current")
    public Long current;
    /**
     * 分页数量
     */
    @JsonProperty("size")
    public Long size;
    /**
     * 总数
     */
    @JsonProperty("total")
    public Long total;
    /**
     * 数据列表
     */
    @JsonProperty("records")
    public List<T> records;

    /**
     * 创建空分页
     *
     * @param current 页码
     * @param size 分页数量
     * @return 空分页
     * @param <T> 数据类型
     */
    public static <T> Pager<T> empty(long current, long size) {
        Pager<T> pager = new Pager<>();
        pager.current = current > 0 ? current : 1L;
        pager.size = size > 0 ? size : 20L;
        pager.total = 0L;
        pager.records = new ArrayList<>();
        return pager;
    }
}
