package xin.manong.hylian.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.config.ServerConfig;
import xin.manong.hylian.model.Profile;
import xin.manong.hylian.server.service.JWTService;

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

    protected final ServerConfig serverConfig;
    protected final Map<String, Algorithm> algorithmMap;
    protected final Map<String, JWTVerifier> verifierMap;

    public JWTServiceImpl(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.algorithmMap = new HashMap<>();
        this.verifierMap = new HashMap<>();
        if (StringUtils.isNotEmpty(serverConfig.jwtConfig.secretHS256)) {
            Algorithm algorithm = Algorithm.HMAC256(serverConfig.jwtConfig.secretHS256);
            algorithmMap.put(Constants.ALGORITHM_HS256, algorithm);
            verifierMap.put(Constants.ALGORITHM_HS256, JWT.require(algorithm).build());
        }
    }

    @Override
    public String buildJWT(Profile profile, Date expiresAt,
                           String algoName, Map<String, Object> headers) {
        if (profile == null) {
            logger.error("profile is null");
            return null;
        }
        if (!algorithmMap.containsKey(algoName)) {
            logger.error("unsupported encrypt algorithm[{}]", algoName);
            return null;
        }
        Algorithm algorithm = algorithmMap.get(algoName);
        return JWT.create().withHeader(headers).withClaim(Constants.JWT_CLAIM_PROFILE, JSON.toJSONString(profile))
                .withExpiresAt(expiresAt).sign(algorithm);
    }

    @Override
    public boolean verify(DecodedJWT decodedJWT) {
        Claim claim = decodedJWT.getHeaderClaim(Constants.JWT_HEADER_ALGORITHM);
        if (claim == null) {
            logger.error("encrypt algorithm is not found");
            return false;
        }
        String algoName = claim.asString();
        if (!verifierMap.containsKey(algoName)) {
            logger.error("unsupported encrypt algorithm[{}] for verifying", algoName);
            return false;
        }
        JWTVerifier verifier = verifierMap.get(algoName);
        try {
            verifier.verify(decodedJWT);
            return true;
        } catch (TokenExpiredException e) {
            logger.error("JWT is expired for time[{}]", decodedJWT.getExpiresAt().getTime());
            logger.error(e.getMessage(), e);
            return false;
        } catch (SignatureVerificationException e){
            logger.error("sign verify failed");
            logger.error(e.getMessage(), e);
            return false;
        } catch (AlgorithmMismatchException e){
            logger.error("algorithm not match");
            logger.error(e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public DecodedJWT decodeJWT(String jwt) {
        try {
            return JWT.decode(jwt);
        } catch (Exception e) {
            logger.error("decode JWT failed");
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Profile decodeProfile(String jwt) {
        DecodedJWT decodedJWT = decodeJWT(jwt);
        if (decodedJWT == null) return null;
        try {
            Claim claim = decodedJWT.getClaim(Constants.JWT_CLAIM_PROFILE);
            if (claim == null) {
                logger.error("claim[{}] is not found", Constants.JWT_CLAIM_PROFILE);
                return null;
            }
            return JSON.parseObject(claim.asString(), Profile.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
