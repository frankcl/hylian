package xin.manong.hylian.server.service;

import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.model.Permission;
import xin.manong.hylian.server.service.request.PermissionSearchRequest;

import java.util.List;

/**
 * 权限服务接口定义
 *
 * @author frankcl
 * @date 2023-10-13 14:04:59
 */
public interface PermissionService {

    /**
     * 根据ID获取权限
     *
     * @param id 权限ID
     * @return 成功返回权限信息，否则返回null
     */
    Permission get(String id);

    /**
     * 批量获取权限
     *
     * @param ids 权限ID列表
     * @return 权限列表
     */
    List<Permission> batchGet(List<String> ids);

    /**
     * 添加权限
     *
     * @param permission 权限信息
     * @return 成功返回true，否则返回false
     */
    boolean add(Permission permission);

    /**
     * 更新权限
     *
     * @param permission 权限信息
     * @return 成功返回true，否则返回false
     */
    boolean update(Permission permission);

    /**
     * 删除权限
     *
     * @param id 权限ID
     * @return 成功返回true，否则返回false
     */
    boolean delete(String id);

    /**
     * 搜索权限信息
     *
     * @param searchRequest 搜索请求
     * @return 权限分页列表
     */
    Pager<Permission> search(PermissionSearchRequest searchRequest);
}
