package xin.manong.security.keeper.model.handler;

import com.alibaba.fastjson.TypeReference;

import java.util.List;

/**
 * JSON字符串列表数据转化器
 *
 * @author frankcl
 * @date 2023-03-15 11:59:24
 */
public class JSONStringListTypeHandler extends JSONListTypeHandler<String> {

    @Override
    protected TypeReference<List<String>> specificType() {
        return new TypeReference<List<String>>() {};
    }
}
