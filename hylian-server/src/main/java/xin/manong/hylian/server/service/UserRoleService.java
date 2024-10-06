package xin.manong.hylian.server.service;

import xin.manong.hylian.model.Pager;
import xin.manong.hylian.model.UserRole;
import xin.manong.hylian.server.service.request.UserRoleSearchRequest;

import java.util.List;

/**
 * 用户角色服务接口定义
 *
 * @author frankcl
 * @date 2023-10-16 11:28:15
 */
public interface UserRoleService {

    /**
     * 添加用户角色关系
     *
     * @param userRole 用户角色关系
     * @return 成功返回true，否则返回false
     */
    boolean add(UserRole userRole);

    /**
     * 获取应用用户所有关系
     *
     * @param appId 应用ID
     * @param userId 用户ID
     * @return 关系列表
     */
    List<UserRole> getByAppUser(String appId, String userId);

    /**
     * 批量更新
     *
     * @param addUserRoles 添加用户角色关系
     * @param removeUserRoles 删除用户角色关系
     */
    void batchUpdate(List<UserRole> addUserRoles, List<Long> removeUserRoles);

    /**
     * 删除用户角色关系
     *
     * @param id 关系ID
     * @return 成功返回true，否则返回false
     */
    boolean delete(Long id);

    /**
     * 删除用户相关所有关系
     *
     * @param userId 用户ID
     */
    void deleteByUser(String userId);

    /**
     * 删除角色相关所有关系
     *
     * @param roleId 角色ID
     */
    void deleteByRole(String roleId);

    /**
     * 搜索用户角色关系
     *
     * @param searchRequest 搜索请求
     * @return 用户角色关系分页列表
     */
    Pager<UserRole> search(UserRoleSearchRequest searchRequest);
}
