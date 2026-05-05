package xin.manong.hylian.server.component;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import xin.manong.hylian.client.config.HylianClientConfig;
import xin.manong.hylian.client.util.CookieUtils;
import xin.manong.hylian.client.util.SessionUtils;
import xin.manong.hylian.model.User;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.config.ServerConfig;
import xin.manong.hylian.server.model.UserProfile;
import xin.manong.weapon.base.util.RandomID;

import java.util.List;

/**
 * 登录管理
 *
 * @author frankcl
 * @date 2026-05-05 11:16:23
 */
@Component
public class LoginManagement {

    @Resource
    private HylianClientConfig hylianClientConfig;
    @Resource
    private ServerConfig serverConfig;
    @Resource
    private ActivityManagement activityManagement;
    @Resource
    private TicketTokenManagement ticketTokenManagement;

    /**
     * 移除用户登录数据
     * 1. 移除ticket和token
     * 2. 移除所有活动信息
     *
     * @param userId 用户ID
     */
    public void sweepLogin(String userId) {
        List<String> tickets = activityManagement.getTicketsByUser(userId);
        if (tickets != null && !tickets.isEmpty()) {
            for (String ticket : tickets) ticketTokenManagement.removeTicketTokensByTicket(ticket);
        }
        activityManagement.removeUserActivities(userId);
    }

    /**
     * 设置用户登录信息
     *
     * @param user 用户信息
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     */
    public void setUserLogin(User user, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        sweepLogin(user.id);
        UserProfile userProfile = new UserProfile();
        userProfile.setId(RandomID.build()).setUserId(user.id);
        String ticket = ticketTokenManagement.buildTicket(userProfile);
        String token = ticketTokenManagement.buildToken(ticket);
        activityManagement.addActivity(userProfile, httpRequest.getSession().getId(), hylianClientConfig.appId);
        SessionUtils.setToken(httpRequest, token);
        CookieUtils.setCookie(Constants.COOKIE_TICKET, ticket, "/",
                serverConfig.domain, true, httpRequest, httpResponse);
        CookieUtils.setCookie(Constants.COOKIE_TOKEN, token, "/",
                serverConfig.domain, false, httpRequest, httpResponse);
        httpResponse.addHeader(Constants.HEADER_TOKEN, token);
    }
}
