package xin.manong.security.keeper.server.service;

import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.UserRole;
import xin.manong.security.keeper.server.service.request.UserRoleSearchRequest;

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
     * 删除用户角色关系
     *
     * @param id 关系ID
     * @return 成功返回true，否则返回false
     */
    boolean delete(Long id);

    /**
     * 搜索用户角色关系
     *
     * @param searchRequest 搜索请求
     * @return 用户角色关系分页列表
     */
    Pager<UserRole> search(UserRoleSearchRequest searchRequest);
}
