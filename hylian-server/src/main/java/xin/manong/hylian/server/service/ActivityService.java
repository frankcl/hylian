package xin.manong.hylian.server.service;

import xin.manong.hylian.model.Activity;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.server.service.request.ActivitySearchRequest;

import java.util.List;

/**
 * 活动记录服务接口定义
 *
 * @author frankcl
 * @date 2023-09-01 19:37:02
 */
public interface ActivityService {

    /**
     * 添加活动记录
     *
     * @param activity 活动记录
     * @return 成功返回true，否则返回false
     */
    boolean add(Activity activity);

    /**
     * 更新活动记录
     *
     * @param activity 活动记录
     * @return 成功返回true，否则返回false
     */
    boolean update(Activity activity);

    /**
     * 清除过期活动记录
     *
     * @param maxUpdateTime 最大更新时间，单位：毫秒
     * @return 清除活动记录数量
     */
    int removeExpires(Long maxUpdateTime);

    /**
     * 移除ticket相关活动记录
     *
     * @param ticketId 票据ID
     */
    void remove(String ticketId);

    /**
     * 移除会话相关活动记录
     *
     * @param sessionId 会话ID
     * @param appId 应用ID
     * @return 成功返回true，否则返回false
     */
    boolean remove(String sessionId, String appId);

    /**
     * 获取应用活动记录
     *
     * @param appId 应用ID
     * @param ticketId 票据ID
     * @return 如果存在返回活动记录，否则返回null
     */
    Activity get(String appId, String ticketId);

    /**
     * 获取ticket相关活动记录
     *
     * @param ticketId 票据ID
     * @return 活动记录列表
     */
    List<Activity> getWithTicket(String ticketId);

    /**
     * 活动记录搜索
     *
     * @param searchRequest 搜索请求
     * @return 活动记录分页列表
     */
    Pager<Activity> search(ActivitySearchRequest searchRequest);

}
