package xin.manong.house.keeper.sso.model.handler;

import com.alibaba.fastjson.TypeReference;
import xin.manong.house.keeper.sso.model.Role;

import java.util.List;

/**
 * JSON URL列表数据转化器
 *
 * @author frankcl
 * @date 2023-03-15 11:59:24
 */
public class JSONRoleListTypeHandler extends AbstractJSONListTypeHandler<Role> {

    @Override
    protected TypeReference<List<Role>> specificType() {
        return new TypeReference<List<Role>>() {};
    }
}
