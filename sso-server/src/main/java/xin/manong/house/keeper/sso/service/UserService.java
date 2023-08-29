package xin.manong.house.keeper.sso.service;

import xin.manong.house.keeper.sso.dao.model.Pager;
import xin.manong.house.keeper.sso.model.User;
import xin.manong.house.keeper.sso.service.request.UserSearchRequest;

/**
 * 用户信息服务接口定义
 *
 * @author frankcl
 * @date 2023-08-29 17:46:50
 */
public interface UserService {

    /**
     * 添加用户信息
     *
     * @param user 用户信息
     * @return 成功返回true，否则返回false
     */
    boolean add(User user);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 成功返回true，否则返回false
     */
    boolean update(User user);

    /**
     * 删除用户信息
     *
     * @param userId 用户ID
     * @return 成功返回true，否则返回false
     */
    boolean delete(String userId);

    /**
     * 搜索用户信息
     *
     * @param searchRequest 搜索请求
     * @return 用户分页列表
     */
    Pager<User> search(UserSearchRequest searchRequest);
}
