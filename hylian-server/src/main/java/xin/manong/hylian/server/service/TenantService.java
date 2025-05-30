package xin.manong.hylian.server.service;

import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.model.Tenant;
import xin.manong.hylian.server.service.request.TenantSearchRequest;

import java.util.List;

/**
 * 租户服务接口定义
 *
 * @author frankcl
 * @date 2023-09-01 13:59:52
 */
public interface TenantService {

    /**
     * 根据ID获取租户信息
     *
     * @param id 租户ID
     * @return 成功返回租户信息，否则返回null
     */
    Tenant get(String id);

    /**
     * 添加租户信息
     *
     * @param tenant 租户信息
     * @return 成功返回true，否则返回false
     */
    boolean add(Tenant tenant);

    /**
     * 更新租户信息
     *
     * @param tenant 租户信息
     * @return 成功返回true，否则返回false
     */
    boolean update(Tenant tenant);

    /**
     * 根据ID删除租户信息
     *
     * @param id 租户ID
     * @return 成功返回true，否则返回false
     */
    boolean delete(String id);

    /**
     * 获取所有租户
     *
     * @return 租户列表
     */
    List<Tenant> getTenants();

    /**
     * 搜索租户信息
     *
     * @param searchRequest 搜索请求
     * @return 租户分页列表
     */
    Pager<Tenant> search(TenantSearchRequest searchRequest);
}
