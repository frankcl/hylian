package xin.manong.hylian.server.component;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.hylian.model.Activity;
import xin.manong.hylian.server.model.UserProfile;
import xin.manong.hylian.server.service.ActivityService;
import xin.manong.hylian.server.service.JWTService;

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
}
