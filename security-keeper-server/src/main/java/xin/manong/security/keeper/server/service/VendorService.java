package xin.manong.security.keeper.server.service;

import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.Vendor;
import xin.manong.security.keeper.server.service.request.VendorSearchRequest;

/**
 * 供应商服务接口定义
 *
 * @author frankcl
 * @date 2023-09-01 13:59:52
 */
public interface VendorService {

    /**
     * 根据ID获取供应商信息
     *
     * @param id 供应商ID
     * @return 成功返回供应商信息，否则返回null
     */
    Vendor get(String id);

    /**
     * 添加供应商信息
     *
     * @param vendor 供应商信息
     * @return 成功返回true，否则返回false
     */
    boolean add(Vendor vendor);

    /**
     * 更新供应商信息
     *
     * @param vendor 供应商信息
     * @return 成功返回true，否则返回false
     */
    boolean update(Vendor vendor);

    /**
     * 根据ID删除供应商信息
     *
     * @param id 供应商ID
     * @return 成功返回true，否则返回false
     */
    boolean delete(String id);

    /**
     * 搜索供应商信息
     *
     * @param searchRequest 搜索请求
     * @return 供应商分页列表
     */
    Pager<Vendor> search(VendorSearchRequest searchRequest);
}
