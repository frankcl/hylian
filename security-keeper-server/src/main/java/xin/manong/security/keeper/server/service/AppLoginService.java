package xin.manong.security.keeper.server.service;

import xin.manong.security.keeper.model.AppLogin;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.server.service.request.AppLoginSearchRequest;

import java.util.List;

/**
 * 应用登录服务接口定义
 *
 * @author frankcl
 * @date 2023-09-01 19:37:02
 */
public interface AppLoginService {

    /**
     * 添加应用登录信息
     *
     * @param appLogin 应用登录信息
     */
    boolean add(AppLogin appLogin);

    /**
     * 清除过期应用登录信息
     *
     * @param maxUpdateTime 最大更新时间，单位：毫秒
     * @return 清除信息数量
     */
    int removeExpiredAppLogins(Long maxUpdateTime);

    /**
     * 移除ticket相关应用登录信息
     *
     * @param ticketId ticket ID
     * @return 成功返回true，否则返回false
     */
    boolean removeAppLogins(String ticketId);

    /**
     * 移除会话相关应用登录信息
     *
     * @param sessionId 会话ID
     * @param appId 应用ID
     * @return 成功返回true，否则返回false
     */
    boolean removeAppLogin(String sessionId, String appId);

    /**
     * 指定用户session是否登录应用
     *
     * @param searchRequest 搜索请求
     * @return 已登录返回true，否则返回false
     */
    boolean isLoginApp(AppLoginSearchRequest searchRequest);

    /**
     * 获取ticket相关应用登录列表
     *
     * @param ticketId ticket ID
     * @return 应用登录列表
     */
    List<AppLogin> getAppLogins(String ticketId);

    /**
     * 应用登录信息搜索
     *
     * @param searchRequest 搜索请求
     * @return 应用登录分页列表
     */
    Pager<AppLogin> search(AppLoginSearchRequest searchRequest);

}
