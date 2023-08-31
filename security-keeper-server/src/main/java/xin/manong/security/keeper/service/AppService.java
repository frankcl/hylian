package xin.manong.security.keeper.service;

import xin.manong.security.keeper.dao.model.Pager;
import xin.manong.security.keeper.model.App;
import xin.manong.security.keeper.service.request.AppSearchRequest;

/**
 * 应用服务接口定义
 *
 * @author frankcl
 * @date 2023-08-30 10:29:16
 */
public interface AppService {

    /**
     * 根据应用ID获取应用信息
     *
     * @param id 应用ID
     * @return 成功返回应用信息，否则返回null
     */
    App get(String id);

    /**
     * 添加应用信息
     *
     * @param app 应用信息
     * @return 成功返回true，否则返回false
     */
    boolean add(App app);

    /**
     * 更新应用信息
     *
     * @param app 应用信息
     * @return 成功返回true，否则返回false
     */
    boolean update(App app);

    /**
     * 删除应用信息
     *
     * @param id 应用ID
     * @return 成功返回true，否则返回false
     */
    boolean delete(String id);

    /**
     * 搜索应用信息
     *
     * @param searchRequest 搜索请求
     * @return 应用分页列表
     */
    Pager<App> search(AppSearchRequest searchRequest);
}
