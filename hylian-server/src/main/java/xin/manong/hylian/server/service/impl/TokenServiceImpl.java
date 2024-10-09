package xin.manong.hylian.server.service.impl;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.codec.digest.DigestUtils;
import org.redisson.api.RBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import xin.manong.hylian.server.model.UserProfile;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.service.JWTService;
import xin.manong.hylian.server.service.TokenService;
import xin.manong.weapon.base.redis.RedisClient;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * token服务实现
 *
 * @author frankcl
 * @date 2023-09-11 10:55:31
 */
@Service
public class TokenServiceImpl implements TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);

    @Resource
    protected RedisClient redisClient;
    @Lazy
    @Resource
    protected JWTService jwtService;

    @Override
    public boolean verifyToken(String token) {
        DecodedJWT decodedJWT = jwtService.decodeJWT(token);
        if (decodedJWT == null) return false;
        Claim claim = decodedJWT.getHeaderClaim(Constants.JWT_HEADER_CATEGORY);
        if (claim == null || !claim.asString().equals(Constants.JWT_CATEGORY_TOKEN)) {
            logger.error("not token for category[{}]", claim == null ? "null" : claim.asString());
            return false;
        }
        return jwtService.verify(decodedJWT);
    }

    @Override
    public String buildToken(UserProfile userProfile, Long expiredTime) {
        Date expiresAt = new Date(System.currentTimeMillis() +
                (expiredTime == null ? Constants.CACHE_TOKEN_EXPIRED_TIME_MS : expiredTime));
        Map<String, Object> headers = new HashMap<>();
        headers.put(Constants.JWT_HEADER_CATEGORY, Constants.JWT_CATEGORY_TOKEN);
        return jwtService.buildJWT(userProfile, expiresAt, Constants.ALGORITHM_HS256, headers);
    }

    @Override
    public String getTicket(String token) {
        String tokenId = DigestUtils.md5Hex(token);
        String key = String.format("%s%s", Constants.TOKEN_CACHE_PREFIX, tokenId);
        RBucket<String> bucket = redisClient.getRedissonClient().getBucket(key);
        return bucket.get();
    }

    @Override
    public void putToken(String token, String ticket) {
        String tokenId = DigestUtils.md5Hex(token);
        String key = String.format("%s%s", Constants.TOKEN_CACHE_PREFIX, tokenId);
        RBucket<String> bucket = redisClient.getRedissonClient().getBucket(key);
        bucket.set(ticket, Constants.CACHE_TOKEN_EXPIRED_TIME_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void removeToken(String token) {
        String tokenId = DigestUtils.md5Hex(token);
        removeTokenWithId(tokenId);
    }

    @Override
    public void removeTokenWithId(String tokenId) {
        String key = String.format("%s%s", Constants.TOKEN_CACHE_PREFIX, tokenId);
        RBucket<String> bucket = redisClient.getRedissonClient().getBucket(key);
        bucket.delete();
    }
}
