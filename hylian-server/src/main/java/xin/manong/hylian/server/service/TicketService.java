package xin.manong.hylian.server.service;

import xin.manong.hylian.model.Profile;

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
     * @param id 票据ID
     * @param ticket ticket
     */
    void putTicket(String id, String ticket);

    /**
     * 移除ticket
     *
     * @param id 票据ID
     */
    void removeTicket(String id);

    /**
     * 获取ticket
     *
     * @param id 票据ID
     * @return 存在返回ticket，否则返回null
     */
    String getTicket(String id);

    /**
     * 为ticket添加关联token
     *
     * @param ticketId 票据ID
     * @param token 关联令牌
     */
    void addToken(String ticketId, String token);

    /**
     * 移除ticket关联token
     *
     * @param ticketId 票据ID
     * @param token 关联令牌
     */
    void removeToken(String ticketId, String token);

    /**
     * 删除ticket关联token
     *
     * @param ticketId 票据ID
     */
    void removeTokens(String ticketId);

    /**
     * 获取ticket关联token集合
     *
     * @param ticketId 票据ID
     * @return 关联令牌集合
     */
    Set<String> getTokens(String ticketId);
}
