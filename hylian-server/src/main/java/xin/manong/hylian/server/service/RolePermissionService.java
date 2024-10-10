package xin.manong.hylian.server.service;

import xin.manong.hylian.model.Permission;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.model.RolePermission;
import xin.manong.hylian.server.service.request.RolePermissionSearchRequest;

import java.util.List;

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
     * 获取角色相关关系列表
     *
     * @param roleId 角色ID
     * @return 关系列表
     */
    List<RolePermission> getByRoleId(String roleId);

    /**
     * 获取角色相关权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByRoleId(String roleId);

    /**
     * 批量更新
     *
     * @param addRolePermissions 添加角色权限关系
     * @param removeRolePermissions 移除角色权限关系
     */
    void batchUpdate(List<RolePermission> addRolePermissions, List<Long> removeRolePermissions);

    /**
     * 删除角色权限关系
     *
     * @param id 关系ID
     * @return 成功返回true，否则返回false
     */
    boolean delete(Long id);

    /**
     * 删除角色相关关系
     *
     * @param roleId 角色ID
     */
    void deleteByRole(String roleId);

    /**
     * 删除权限相关关系
     *
     * @param permissionId 权限ID
     */
    void deleteByPermission(String permissionId);

    /**
     * 搜索角色权限关系
     *
     * @param searchRequest 搜索请求
     * @return 角色权限关系分页列表
     */
    Pager<RolePermission> search(RolePermissionSearchRequest searchRequest);
}
