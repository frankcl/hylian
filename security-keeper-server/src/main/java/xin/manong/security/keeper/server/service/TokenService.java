package xin.manong.security.keeper.server.service;

import xin.manong.security.keeper.model.Profile;

/**
 * token服务接口定义
 *
 * @author frankcl
 * @date 2023-09-01 16:27:05
 */
public interface TokenService {

    /**
     * 验证token
     *
     * @param token
     * @return 有效返回true，否则返回false
     */
    boolean verifyToken(String token);

    /**
     * 构建token
     *
     * @param profile 用户信息
     * @param expiredTime 过期时间，单位：毫秒
     * @return 成功返回token，否则返回null
     */
    String buildToken(Profile profile, Long expiredTime);

    /**
     * 根据token获取ticket
     *
     * @param token
     * @return 存在返回ticket，否则返回null
     */
    String getTicket(String token);

    /**
     * 添加token和ticket映射
     *
     * @param token
     * @param ticket
     */
    void putTokenTicket(String token, String ticket);

    /**
     * 移除token和ticket映射
     *
     * @param token
     */
    void removeTokenTicket(String token);

    /**
     * 移除token和ticket映射
     *
     * @param tokenId token ID
     */
    void removeTokenTicketById(String tokenId);
}
