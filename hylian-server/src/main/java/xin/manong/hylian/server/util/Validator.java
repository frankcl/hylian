package xin.manong.hylian.server.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.hylian.model.BaseModel;
import xin.manong.hylian.model.util.ModelValidator;
import xin.manong.hylian.server.service.request.OrderByRequest;
import xin.manong.hylian.server.service.request.SearchRequest;

import javax.ws.rs.BadRequestException;
import java.util.List;

/**
 * 数据验证
 *
 * @author frankcl
 * @date 2024-10-03 20:38:40
 */
public class Validator {

    private static final Logger logger = LoggerFactory.getLogger(Validator.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 验证排序字段合法性
     * 非法抛出异常 BadRequestException
     *
     * @param model 数据模型类
     * @param searchRequest 搜索条件
     */
    public static void validateOrderBy(Class<? extends BaseModel> model, SearchRequest searchRequest) {
        if (StringUtils.isEmpty(searchRequest.orderBy)) return;
        try {
            searchRequest.orderByRequests = objectMapper.readValue(searchRequest.orderBy,
                    new TypeReference<List<OrderByRequest>>() { });
        } catch (Exception e) {
            logger.error("invalid order by[{}]", searchRequest.orderBy);
            throw new BadRequestException("排序字段非法");
        }
        for (OrderByRequest orderBy : searchRequest.orderByRequests) {
            ModelValidator.validateField(model, orderBy.field);
        }
    }
}
