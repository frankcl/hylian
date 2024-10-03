package xin.manong.hylian.server.util;

import xin.manong.hylian.model.BaseModel;
import xin.manong.hylian.model.util.ModelValidator;
import xin.manong.hylian.server.service.request.OrderByRequest;
import xin.manong.hylian.server.service.request.SearchRequest;

/**
 * 数据验证
 *
 * @author frankcl
 * @date 2024-10-03 20:38:40
 */
public class Validator {

    /**
     * 验证排序字段合法性
     * 非法抛出异常 BadRequestException
     *
     * @param model 数据模型类
     * @param searchRequest 搜索条件
     */
    public static void validateOrderBy(Class<? extends BaseModel> model, SearchRequest searchRequest) {
        if (searchRequest.orderBy == null) return;
        for (OrderByRequest orderBy : searchRequest.orderBy) {
            ModelValidator.validateField(model, orderBy.field);
        }
    }
}
