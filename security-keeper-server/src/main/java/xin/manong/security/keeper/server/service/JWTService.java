package xin.manong.security.keeper.server.service;

import xin.manong.security.keeper.model.Profile;

/**
 * JWT服务接口定义
 *
 * @author frankcl
 * @date 2023-08-31 17:24:33
 */
public interface JWTService {

    /**
     * 构建Ticket
     *
     * @param profile 用户简介
     * @param algorithm 加密算法
     * @param expiredTime 过期时间间隔，单位毫秒
     * @return ticket
     */
    String buildTicket(Profile profile, String algorithm, Long expiredTime);

    /**
     * 构建Token
     *
     * @param profile 用户简介
     * @param algorithm 加密算法
     * @param expiredTime 过期时间间隔，单位毫秒
     * @return token
     */
    String buildToken(Profile profile, String algorithm, Long expiredTime);

    /**
     * 根据ticket创建token
     *
     * @param ticket
     * @param expiredTime 过期时间间隔，单位毫秒
     * @return 成功返回token，否则返回null
     */
    String buildTokenWithTicket(String ticket, Long expiredTime);

    /**
     * 验证ticket有效性
     *
     * @param ticket ticket
     * @return 有效返回true，否则返回false
     */
    boolean verifyTicket(String ticket);

    /**
     * 验证token有效性
     *
     * @param token token
     * @return 有效返回true，否则返回false
     */
    boolean verifyToken(String token);

    /**
     * 从JWT中获取用户简介
     *
     * @param token JWT
     * @return 成功返回用户简介，否则返回null
     */
    Profile decodeProfile(String token);
}
