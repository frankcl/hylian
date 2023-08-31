package xin.manong.security.keeper.service;

import xin.manong.security.keeper.model.Profile;

/**
 * JWT服务接口定义
 *
 * @author frankcl
 * @date 2023-08-31 17:24:33
 */
public interface JWTService {

    /**
     * 构建JWT
     *
     * @param profile 用户简介
     * @param expiredTime 过期时间间隔，单位毫秒
     * @return JWT
     */
    String buildJWT(Profile profile, Long expiredTime);

    /**
     * 验证token有效性
     *
     * @param token JWT
     * @return 有效返回true，否则返回false
     */
    boolean verify(String token);

    /**
     * 从JWT中获取用户简介
     *
     * @param token JWT
     * @return 成功返回用户简介，否则返回null
     */
    Profile decodeProfile(String token);
}
