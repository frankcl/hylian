package xin.manong.security.keeper.server.service.impl;

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
import xin.manong.security.keeper.server.common.Constants;
import xin.manong.security.keeper.server.config.ServerConfig;
import xin.manong.security.keeper.model.Profile;
import xin.manong.security.keeper.server.service.JWTService;

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

    private static final Set<String> SUPPORT_ALGORITHMS = new HashSet<String>() {
        { add(Constants.ALGORITHM_HS256); }
    };

    @Resource
    protected ServerConfig serverConfig;

    @Override
    public String buildJWT(Profile profile, Date expiresAt,
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
        if (algoName.equals(Constants.ALGORITHM_HS256)) {
            algorithm = Algorithm.HMAC256(serverConfig.jwtConfig.getSecretHS256());
        }
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
        if (!SUPPORT_ALGORITHMS.contains(algoName)) {
            logger.error("unsupported encrypt algorithm[{}]", algoName);
            return false;
        }
        Algorithm algorithm = null;
        if (algoName.equals(Constants.ALGORITHM_HS256)) {
            algorithm = Algorithm.HMAC256(serverConfig.jwtConfig.getSecretHS256());
        }
        JWTVerifier verifier = JWT.require(algorithm).build();
        try {
            verifier.verify(decodedJWT);
            return true;
        } catch (TokenExpiredException e) {
            logger.error("JWT is expired for time[{}]", decodedJWT.getExpiresAt().getTime());
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
