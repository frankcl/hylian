package xin.manong.hylian.server.service.impl;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.codec.digest.DigestUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import xin.manong.hylian.server.model.UserProfile;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.service.JWTService;
import xin.manong.hylian.server.service.TicketService;
import xin.manong.weapon.base.redis.RedisClient;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * ticket服务实现
 *
 * @author frankcl
 * @date 2023-09-12 16:27:33
 */
@Service
public class TicketServiceImpl implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

    @Resource
    protected RedisClient redisClient;
    @Lazy
    @Resource
    protected JWTService jwtService;

    @Override
    public String buildTicket(UserProfile userProfile, Long expiredTime) {
        Date expiresAt = new Date(System.currentTimeMillis() +
                (expiredTime == null ? Constants.COOKIE_TICKET_EXPIRED_TIME_MS : expiredTime));
        Map<String, Object> headers = new HashMap<>();
        headers.put(Constants.JWT_HEADER_CATEGORY, Constants.JWT_CATEGORY_TICKET);
        return jwtService.buildJWT(userProfile, expiresAt, Constants.ALGORITHM_HS256, headers);
    }

    @Override
    public boolean verifyTicket(String ticket) {
        DecodedJWT decodedJWT = jwtService.decodeJWT(ticket);
        if (decodedJWT == null) return false;
        Claim claim = decodedJWT.getHeaderClaim(Constants.JWT_HEADER_CATEGORY);
        if (claim == null || !claim.asString().equals(Constants.JWT_CATEGORY_TICKET)) {
            logger.error("not ticket for category[{}]", claim == null ? "null" : claim.asString());
            return false;
        }
        return jwtService.verify(decodedJWT);
    }

    @Override
    public void putTicket(String id, String ticket) {
        String key = String.format("%s%s", Constants.TICKET_CACHE_PREFIX, id);
        RBucket<String> bucket = redisClient.getRedissonClient().getBucket(key);
        bucket.set(ticket, Constants.CACHE_TICKET_EXPIRED_TIME_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void removeTicket(String id) {
        String key = String.format("%s%s", Constants.TICKET_CACHE_PREFIX, id);
        RBucket<String> bucket = redisClient.getRedissonClient().getBucket(key);
        bucket.delete();
    }

    @Override
    public String getTicket(String id) {
        String key = String.format("%s%s", Constants.TICKET_CACHE_PREFIX, id);
        RBucket<String> bucket = redisClient.getRedissonClient().getBucket(key);
        return bucket.get();
    }

    @Override
    public void addToken(String ticketId, String token) {
        String key = String.format("%s%s", Constants.TICKET_TOKEN_PREFIX, ticketId);
        String tokenId = DigestUtils.md5Hex(token);
        RSet<String> tokens = redisClient.getRedissonClient().getSet(key);
        tokens.add(tokenId);
        tokens.expire(Duration.ofMillis(Constants.CACHE_TOKEN_EXPIRED_TIME_MS));
    }

    @Override
    public void removeToken(String ticketId, String token) {
        String key = String.format("%s%s", Constants.TICKET_TOKEN_PREFIX, ticketId);
        String tokenId = DigestUtils.md5Hex(token);
        RSet<String> tokens = redisClient.getRedissonClient().getSet(key);
        tokens.remove(tokenId);
    }

    @Override
    public void removeTokens(String ticketId) {
        String key = String.format("%s%s", Constants.TICKET_TOKEN_PREFIX, ticketId);
        RSet<String> tokens = redisClient.getRedissonClient().getSet(key);
        tokens.delete();
    }

    @Override
    public Set<String> getTokens(String ticketId) {
        String key = String.format("%s%s", Constants.TICKET_TOKEN_PREFIX, ticketId);
        RSet<String> tokens = redisClient.getRedissonClient().getSet(key);
        return new HashSet<>(tokens);
    }
}
