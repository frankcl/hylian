package xin.manong.hylian.model.util;

import com.baomidou.mybatisplus.annotation.TableField;
import xin.manong.hylian.model.BaseModel;
import xin.manong.weapon.base.util.ReflectUtil;

import javax.ws.rs.BadRequestException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据模型检测
 *
 * @author frankcl
 * @date 2024-10-03 19:43:37
 */
public class ModelValidator {

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
}
