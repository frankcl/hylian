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
import java.util.Date;

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

    private static final Long DEFAULT_TOKEN_EXPIRED_TIME_MS = 300000L;

    @Resource
    protected ServerConfig serverConfig;

    @Override
    public String buildJWT(Profile profile, Long expireTime) {
        if (profile == null) {
            logger.error("profile is null");
            return null;
        }
        Date expiresAt = new Date(System.currentTimeMillis() +
                (expireTime == null ? DEFAULT_TOKEN_EXPIRED_TIME_MS : expireTime));
        Algorithm algorithm = Algorithm.HMAC256(serverConfig.jwtConfig.getSecretHS256());
        return JWT.create().withClaim(CLAIM_KEY_PROFILE, JSON.toJSONString(profile))
                .withExpiresAt(expiresAt).sign(algorithm);
    }

    @Override
    public boolean verify(String token) {
        Algorithm algorithm = Algorithm.HMAC256(serverConfig.jwtConfig.getSecretHS256());
        JWTVerifier verifier = JWT.require(algorithm).build();
        try {
            verifier.verify(token);
            return true;
        } catch (TokenExpiredException e) {
            logger.error("token is expired");
            return false;
        } catch (SignatureVerificationException e){
            logger.error("sign verify failed");
            return false;
        } catch (AlgorithmMismatchException e){
            logger.error("algorithm not match");
            return false;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
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
