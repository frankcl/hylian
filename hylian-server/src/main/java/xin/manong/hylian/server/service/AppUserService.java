package xin.manong.hylian.server.service;

import xin.manong.hylian.model.AppUser;
import xin.manong.hylian.model.User;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.server.service.request.AppUserSearchRequest;

import java.util.List;

/**
 * 应用用户关系服务接口
 *
 * @author frankcl
 * @date 2024-10-13 18:34:54
 */
public interface AppUserService {

    /**
     * 添加应用用户关系
     *
     * @param appUser 应用用户关系
     * @return 成功返回true，否则返回false
     */
    boolean add(AppUser appUser);

    /**
     * 获取指定应用用户关系
     *
     * @param appId 应用ID
     * @param userId 用户ID
     * @return 成功返回关系，否则返回null
     */
    AppUser getAppUser(String appId, String userId);

    /**
     * 获取应用所有关系
     *
     * @param appId 应用ID
     * @return 关系列表
     */
    List<AppUser> getByAppId(String appId);

    /**
     * 获取用户所有关系
     *
     * @param userId 用户ID
     * @return 关系列表
     */
    List<AppUser> getByUserId(String userId);

    /**
     * 获取应用所有用户
     *
     * @param appId 应用ID
     * @return 用户列表
     */
    List<User> getUsersByApp(String appId);

    /**
     * 批量更新
     *
     * @param addAppUsers 添加应用用户关系
     * @param removeAppUsers 移除应用用户关系
     */
    void batchUpdate(List<AppUser> addAppUsers, List<Long> removeAppUsers);

    /**
     * 删除应用用户关系
     *
     * @param id 关系ID
     * @return 成功返回true，否则返回false
     */
    boolean delete(Long id);

    /**
     * 删除应用所有关系
     *
     * @param appId 应用ID
     */
    void deleteByApp(String appId);

    /**
     * 删除用户所有关系
     *
     * @param userId 用户ID
     */
    void deleteByUser(String userId);

    /**
     * 搜索应用用户关系
     *
     * @param searchRequest 搜索请求
     * @return 应用用户关系分页列表
     */
    Pager<AppUser> search(AppUserSearchRequest searchRequest);
}
