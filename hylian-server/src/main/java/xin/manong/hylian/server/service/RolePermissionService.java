package xin.manong.hylian.server.service;

import xin.manong.hylian.model.Pager;
import xin.manong.hylian.model.RolePermission;
import xin.manong.hylian.server.service.request.RolePermissionSearchRequest;

/**
 * 角色权限关系服务接口定义
 *
 * @author frankcl
 * @date 2023-10-16 11:34:29
 */
public interface RolePermissionService {

    /**
     * 添加角色权限关系
     *
     * @param rolePermission 角色权限关系
     * @return 成功返回true，否则返回false
     */
    boolean add(RolePermission rolePermission);

    /**
     * 删除角色权限关系
     *
     * @param id 关系ID
     * @return 成功返回true，否则返回false
     */
    boolean delete(Long id);

    /**
     * 搜索角色权限关系
     *
     * @param searchRequest 搜索请求
     * @return 角色权限关系分页列表
     */
    Pager<RolePermission> search(RolePermissionSearchRequest searchRequest);
}
