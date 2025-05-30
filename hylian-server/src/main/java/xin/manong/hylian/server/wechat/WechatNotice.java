package xin.manong.hylian.server.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import xin.manong.weapon.base.util.ReflectUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信通知对象
 *
 * @author frankcl
 * @date 2025-05-29 22:22:38
 */
public class WechatNotice {

    /**
     * 转换Map对象
     *
     * @return Map对象
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        List<Field> fields = ReflectUtil.getAnnotatedFields(this.getClass(), JsonProperty.class);
        for (Field field : fields) {
            JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
            Map<String, Object> innerMap = new HashMap<>();
            innerMap.put("value", ReflectUtil.getFieldValue(this, field.getName()));
            map.put(jsonProperty.value(), innerMap);
        }
        return map;
    }
}
