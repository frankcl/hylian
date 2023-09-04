package xin.manong.security.keeper.server.service;

import xin.manong.security.keeper.model.LoginApp;

import java.util.List;

/**
 * 登录应用服务接口定义
 *
 * @author frankcl
 * @date 2023-09-01 19:37:02
 */
public interface LoginAppService {

    /**
     * 注册登录应用信息
     *
     * @param loginApp 登录应用信息
     */
    boolean register(LoginApp loginApp);

    /**
     * 移除ticket相关用户登录应用
     *
     * @param ticketId ticket ID
     */
    void removeLoginApps(String ticketId);

    /**
     * 指定用户应用是否已经登录
     *
     * @param ticketId ticket ID
     * @param appId 应用ID
     * @return 已登录返回true，否则返回false
     */
    boolean isLoginApp(String ticketId, String appId);

    /**
     * 获取用户相关ticket登录应用列表
     *
     * @param ticketId ticket ID
     * @return 登录应用列表
     */
    List<LoginApp> getLoginApps(String ticketId);

}
