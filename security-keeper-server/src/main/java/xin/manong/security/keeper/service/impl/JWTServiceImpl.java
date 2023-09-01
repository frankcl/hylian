package xin.manong.security.keeper.service.impl;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.security.keeper.config.ServerConfig;
import xin.manong.security.keeper.model.Profile;
import xin.manong.security.keeper.service.JWTService;

import javax.annotation.Resource;
import java.util.*;

/**
 * JWT服务实现
 *
 * @author frankcl
 * @date 2023-08-31 17:39:17
 */
@Service
public class JWTServiceImpl implements JWTService {

    private static final Logger logger = LoggerFactory.getLogger(JWTServiceImpl.class);

    private static final String CLAIM_KEY_PROFILE = "profile";
    private static final String HEADER_KEY_CATEGORY = "category";
    private static final String HEADER_KEY_ALGORITHM = "alg";

    private static final String CATEGORY_TICKET = "ticket";
    private static final String CATEGORY_TOKEN = "token";

    private static final Long DEFAULT_TOKEN_EXPIRED_TIME_MS = 300000L;
    private static final Long DEFAULT_TICKET_EXPIRED_TIME_MS = 86400000L;

    public static final String ALGORITHM_HS256 = "HS256";

    private static final Set<String> SUPPORT_ALGORITHMS = new HashSet<String>() {
        { add(ALGORITHM_HS256); }
    };

    @Resource
    protected ServerConfig serverConfig;

    @Override
    public String buildTicket(Profile profile, String algorithm, Long expiredTime) {
        Date expiresAt = new Date(System.currentTimeMillis() +
                (expiredTime == null ? DEFAULT_TICKET_EXPIRED_TIME_MS : expiredTime));
        Map<String, Object> headers = new HashMap<>();
        headers.put(HEADER_KEY_CATEGORY, CATEGORY_TICKET);
        return buildJWT(profile, expiresAt, algorithm, headers);
    }

    @Override
    public String buildToken(Profile profile, String algorithm, Long expiredTime) {
        Date expiresAt = new Date(System.currentTimeMillis() +
                (expiredTime == null ? DEFAULT_TOKEN_EXPIRED_TIME_MS : expiredTime));
        Map<String, Object> headers = new HashMap<>();
        headers.put(HEADER_KEY_CATEGORY, CATEGORY_TOKEN);
        return buildJWT(profile, expiresAt, algorithm, headers);
    }

    @Override
    public boolean verifyTicket(String ticket) {
        DecodedJWT decodedJWT = decode(ticket);
        if (decodedJWT == null) return false;
        Claim claim = decodedJWT.getHeaderClaim(HEADER_KEY_CATEGORY);
        if (claim == null || !claim.asString().equals(CATEGORY_TICKET)) {
            logger.error("not ticket for category[{}]", claim == null ? "null" : claim.asString());
            return false;
        }
        return verify(decodedJWT);
    }

    @Override
    public boolean verifyToken(String token) {
        DecodedJWT decodedJWT = decode(token);
        if (decodedJWT == null) return false;
        Claim claim = decodedJWT.getHeaderClaim(HEADER_KEY_CATEGORY);
        if (claim == null || !claim.asString().equals(CATEGORY_TOKEN)) {
            logger.error("not token for category[{}]", claim == null ? "null" : claim.asString());
            return false;
        }
        return verify(decodedJWT);
    }

    @Override
    public Profile decodeProfile(String token) {
        DecodedJWT decodedJWT = decode(token);
        if (decodedJWT == null) return null;
        try {
            Claim claim = decodedJWT.getClaim(CLAIM_KEY_PROFILE);
            if (claim == null) {
                logger.error("claim[{}] is not found", CLAIM_KEY_PROFILE);
                return null;
            }
            return JSON.parseObject(claim.asString(), Profile.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 构建JWT
     *
     * @param profile 用户信息
     * @param expiresAt 过期时间
     * @param algoName 加密算法
     * @param headers JWT headers
     * @return 成功返回JWT，否则返回null
     */
    private String buildJWT(Profile profile, Date expiresAt,
                            String algoName, Map<String, Object> headers) {
        if (profile == null) {
            logger.error("profile is null");
            return null;
        }
        if (!SUPPORT_ALGORITHMS.contains(algoName)) {
            logger.error("unsupported encrypt algorithm[{}]", algoName);
            return null;
        }
        Algorithm algorithm = null;
        if (algoName.equals(ALGORITHM_HS256)) algorithm = Algorithm.HMAC256(serverConfig.jwtConfig.getSecretHS256());
        return JWT.create().withHeader(headers).withClaim(CLAIM_KEY_PROFILE, JSON.toJSONString(profile))
                .withExpiresAt(expiresAt).sign(algorithm);
    }

    /**
     * 验证token有效性
     *
     * @param decodedJWT
     * @return 有效返回true，否则返回false
     */
    private boolean verify(DecodedJWT decodedJWT) {
        Claim claim = decodedJWT.getHeaderClaim(HEADER_KEY_ALGORITHM);
        if (claim == null) {
            logger.error("encrypt algorithm is not found");
            return false;
        }
        String algoName = claim.asString();
        if (!SUPPORT_ALGORITHMS.contains(algoName)) {
            logger.error("unsupported encrypt algorithm[{}]", algoName);
            return false;
        }
        Algorithm algorithm = null;
        if (algoName.equals(ALGORITHM_HS256)) algorithm = Algorithm.HMAC256(serverConfig.jwtConfig.getSecretHS256());
        JWTVerifier verifier = JWT.require(algorithm).build();
        try {
            verifier.verify(decodedJWT);
            return true;
        } catch (TokenExpiredException e) {
            logger.error("token is expired, time[{}]", decodedJWT.getExpiresAt().getTime());
            return false;
        } catch (SignatureVerificationException e){
            logger.error("sign verify failed");
            return false;
        } catch (AlgorithmMismatchException e){
            logger.error("algorithm[{}] not match for expected[{}]", algorithm.getName(), algoName);
            return false;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 解码JWT
     *
     * @param token JWT
     * @return 成功返回解码对象，否则返回null
     */
    private DecodedJWT decode(String token) {
        try {
            return JWT.decode(token);
        } catch (Exception e) {
            logger.error("decode token failed");
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
