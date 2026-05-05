package xin.manong.hylian.server.component;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.hylian.model.Activity;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.server.model.UserProfile;
import xin.manong.hylian.server.service.ActivityService;
import xin.manong.hylian.server.service.JWTService;
import xin.manong.hylian.server.service.request.ActivitySearchRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户活动管理
 *
 * @author frankcl
 * @date 2026-04-09 13:55:01
 */
@Component
public class ActivityManagement {

    private static final Logger logger = LoggerFactory.getLogger(ActivityManagement.class);

    @Resource
    private JWTService jwtService;
    @Resource
    private ActivityService activityService;

    /**
     * 添加活动记录
     *
     * @param userProfile 用户信息
     * @param sessionId 会话ID
     * @param appId 应用ID
     */
    public void addActivity(UserProfile userProfile, String sessionId, String appId) {
        Activity activity = Activity.builder().userId(userProfile.userId).
                ticketId(userProfile.id).sessionId(sessionId).appId(appId).build();
        if (!activityService.upsert(activity)) {
            logger.warn("Upsert activity failed for app:{} and user:{}" , appId, userProfile.userId);
        }
    }

    /**
     * 移除应用会话活动
     *
     * @param appId 应用ID
     * @param sessionId 会话ID
     */
    public boolean removeActivity(String appId, String sessionId) {
        return activityService.remove(sessionId, appId);
    }

    /**
     * 移除ticket相关活动
     *
     * @param ticket 票据
     */
    public void removeActivity(String ticket) {
        UserProfile userProfile = jwtService.decodeProfile(ticket);
        if (userProfile != null) activityService.remove(userProfile.id);
    }

    /**
     * 移除用户所有活动记录
     *
     * @param userId 用户ID
     */
    public void removeUserActivities(String userId) {
        if (!activityService.removeByUserId(userId)) logger.warn("Remove activities failed for user:{}", userId);
    }

    /**
     * 获取用户所有登录ticket
     *
     * @param userId 用户ID
     * @return ticket列表
     */
    public List<String> getTicketsByUser(String userId) {
        ActivitySearchRequest searchRequest = new ActivitySearchRequest();
        searchRequest.userId = userId;
        searchRequest.pageSize = 100;
        Pager<Activity> pager = activityService.search(searchRequest);
        return pager.records.stream().map(activity -> activity.ticketId).
                filter(StringUtils::isNotEmpty).collect(Collectors.toSet()).stream().toList();
    }
}
