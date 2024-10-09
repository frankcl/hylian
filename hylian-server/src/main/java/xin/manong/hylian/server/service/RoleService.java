package xin.manong.hylian.server.service;

import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.model.Role;
import xin.manong.hylian.server.service.request.RoleSearchRequest;

import java.util.List;

/**
 * 角色服务接口定义
 *
 * @author frankcl
 * @date 2023-10-13 15:16:00
 */
public interface RoleService {

    /**
     * 根据ID获取角色
     *
     * @param id 角色ID
     * @return 成功返回角色信息，否则返回null
     */
    Role get(String id);

    /**
     * 批量获取角色
     *
     * @param ids 角色ID列表
     * @return 角色列表
     */
    List<Role> batchGet(List<String> ids);

    /**
     * 添加角色
     *
     * @param role 角色信息
     * @return 成功返回true，否则返回false
     */
    boolean add(Role role);

    /**
     * 更新角色
     *
     * @param role 角色信息
     * @return 成功返回true，否则返回false
     */
    boolean update(Role role);

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 成功返回true，否则返回false
     */
    boolean delete(String id);

    /**
     * 搜索角色信息
     *
     * @param searchRequest 搜索请求
     * @return 角色分页列表
     */
    Pager<Role> search(RoleSearchRequest searchRequest);
}
