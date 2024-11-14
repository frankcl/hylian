package xin.manong.hylian.server.util;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.BadRequestException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.hylian.model.BaseModel;
import xin.manong.hylian.server.service.request.OrderByRequest;
import xin.manong.hylian.server.service.request.SearchRequest;
import xin.manong.weapon.base.util.ReflectUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据模型验证
 *
 * @author frankcl
 * @date 2024-10-03 20:38:40
 */
public class ModelValidator {

    private static final Logger logger = LoggerFactory.getLogger(ModelValidator.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<Class<? extends BaseModel>, List<Field>> modelFieldMap = new ConcurrentHashMap<>();

    /**
     * 验证数据模型是否有字段
     * 如果没有字段抛出异常 BadRequestException
     *
     * @param model 数据模型类
     * @param fieldName 字段名称
     */
    public static void validateField(Class<? extends BaseModel> model, String fieldName) {
        List<Field> fields = modelFieldMap.get(model);
        if (fields == null) {
            fields = ReflectUtil.getAnnotatedFields(model, TableField.class);
            modelFieldMap.put(model, fields);
        }
        for (Field field : fields) {
            TableField annotation = ReflectUtil.getFieldAnnotation(field, TableField.class);
            if (annotation.value().equals(fieldName)) return;
        }
        throw new BadRequestException(String.format("非法字段：%s", fieldName));
    }

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
            validateField(model, orderBy.field);
        }
    }

    /**
     * 验证转换列表字段
     *
     * @param fieldValue 字符串形式列表字段
     * @param recordType 列表数据类型
     * @return 列表数据
     * @param <T> 列表数据类型
     */
    public static <T> List<T> validateListField(String fieldValue, Class<T> recordType) {
        try {
            if (StringUtils.isEmpty(fieldValue)) return null;
            return objectMapper.readValue(fieldValue, new TypeReference<List<T>>() { });
        } catch (Exception e) {
            logger.error("invalid List field[{}] for record[{}]", fieldValue, recordType.getName());
            throw new BadRequestException("列表字段非法");
        }
    }
}
