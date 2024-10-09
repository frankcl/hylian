package xin.manong.hylian.server.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import xin.manong.hylian.server.model.UserProfile;

import java.util.Date;
import java.util.Map;

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
     * @param userProfile 用户信息
     * @param expiresAt 过期时间
     * @param algorithm 加密算法
     * @param headers JWT headers
     * @return 成功返回JWT，否则返回null
     */
    String buildJWT(UserProfile userProfile, Date expiresAt,
                    String algorithm, Map<String, Object> headers);

    /**
     * 从JWT中解码用户信息
     *
     * @param jwt JWT
     * @return 成功返回用户信息，否则返回null
     */
    UserProfile decodeProfile(String jwt);

    /**
     * 解码JWT
     *
     * @param jwt JWT
     * @return 成功返回JWT解码对象，否则返回null
     */
    DecodedJWT decodeJWT(String jwt);

    /**
     * 验证JWT有效性
     *
     * @param decodedJWT 解码JWT
     * @return 有效返回true，否则返回false
     */
    boolean verify(DecodedJWT decodedJWT);
}
