package xin.manong.hylian.server.service;

import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.model.User;
import xin.manong.hylian.server.service.request.UserSearchRequest;

import java.util.List;

/**
 * 用户信息服务接口定义
 *
 * @author frankcl
 * @date 2023-08-29 17:46:50
 */
public interface UserService {

    /**
     * 根据用户ID获取用户信息
     *
     * @param id 用户ID
     * @return 成功返回用户信息，否则返回null
     */
    User get(String id);

    /**
     * 根据微信UID获取用户信息
     *
     * @param wxOpenid 微信小程序openid
     * @return 成功返回用户信息，否则返回null
     */
    User getByWxOpenid(String wxOpenid);

    /**
     * 批量获取用户
     *
     * @param ids 用户ID列表
     * @return 用户列表
     */
    List<User> batchGet(List<String> ids);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 成功返回用户信息，否则返回null
     */
    User getByUserName(String username);

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
     * @param id 用户ID
     * @return 成功返回true，否则返回false
     */
    boolean delete(String id);

    /**
     * 搜索用户信息
     *
     * @param searchRequest 搜索请求
     * @return 用户分页列表
     */
    Pager<User> search(UserSearchRequest searchRequest);

    /**
     * 移除用户登录信息
     * 1. ticket相关信息
     * 2. token相关信息
     *
     * @param id profile id
     */
    void removeUserProfile(String id);
}
