package xin.manong.security.keeper.server.service;

import xin.manong.security.keeper.model.Profile;

import java.util.Set;

/**
 * ticket服务接口定义
 *
 * @author frankcl
 * @date 2023-09-12 16:18:59
 */
public interface TicketService {

    /**
     * 构建ticket
     *
     * @param profile 用户信息
     * @param expiredTime 过期时间，单位：毫秒
     * @return 成功返回ticket，否则返回null
     */
    String buildTicket(Profile profile, Long expiredTime);

    /**
     * 验证ticket
     *
     * @param ticket 票据
     * @return 有效返回true，否则返回false
     */
    boolean verifyTicket(String ticket);

    /**
     * 添加ticket
     *
     * @param id ticket ID
     * @param ticket ticket
     */
    void putTicket(String id, String ticket);

    /**
     * 移除ticket
     *
     * @param id ticket ID
     */
    void removeTicket(String id);

    /**
     * 获取ticket
     *
     * @param id ticket ID
     * @return 存在返回ticket，否则返回null
     */
    String getTicket(String id);

    /**
     * 为ticket添加关联token
     *
     * @param ticketId ticket ID
     * @param token 关联token
     */
    void addToken(String ticketId, String token);

    /**
     * 移除ticket关联token
     *
     * @param ticketId ticket ID
     * @param token 关联token
     */
    void removeToken(String ticketId, String token);

    /**
     * 删除ticket关联token
     *
     * @param ticketId ticket ID
     */
    void removeTicketTokens(String ticketId);

    /**
     * 获取ticket关联token集合
     *
     * @param ticketId ticket ID
     * @return 关联token集合
     */
    Set<String> getTicketTokens(String ticketId);
}
