package xin.manong.security.keeper.server.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.DeletedObjectListener;
import org.redisson.api.ExpiredObjectListener;
import org.redisson.api.RBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xin.manong.security.keeper.server.common.Constants;
import xin.manong.security.keeper.server.config.ServerConfig;
import xin.manong.security.keeper.server.service.CodeService;
import xin.manong.weapon.base.redis.RedisClient;
import xin.manong.weapon.base.util.RandomID;
import xin.manong.weapon.base.util.ShortKeyBuilder;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 安全码服务实现
 *
 * @author frankcl
 * @date 2023-09-01 16:31:52
 */
@Service
public class CodeServiceImpl implements CodeService {

    private static final Logger logger = LoggerFactory.getLogger(CodeServiceImpl.class);

    @Resource
    protected RedisClient redisClient;
    protected Cache<String, RBucket<String>> cache;
    private ServerConfig serverConfig;

    @Autowired
    public CodeServiceImpl(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        CacheBuilder<String, RBucket<String>> builder = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .maximumSize(serverConfig.securityCodeConfig.cacheCapacity)
                .expireAfterAccess(5, TimeUnit.SECONDS)
                .removalListener(n -> onRemoval(n));
        cache = builder.build();
    }

    @Override
    public String createCode(String ticket) {
        String code;
        RBucket<String> bucket;
        while (true) {
            code = ShortKeyBuilder.build(RandomID.build());
            if (StringUtils.isEmpty(code)) {
                logger.error("create code failed for ticket[{}]", ticket);
                throw new RuntimeException("创建安全码失败");
            }
            String key = String.format("%s%s", Constants.CODE_CACHE_PREFIX, code);
            bucket = redisClient.getRedissonClient().getBucket(key);
            if (bucket.get() == null) break;
        }
        bucket.set(ticket, serverConfig.securityCodeConfig.expiredTimeSeconds, TimeUnit.SECONDS);
        bucket.addListener((ExpiredObjectListener) name -> {
            logger.info("code[{}] is expired", name);
            removeCache(name);
        });
        bucket.addListener((DeletedObjectListener) name -> {
            logger.info("code[{}] is deleted", name);
            removeCache(name);
        });
        cache.put(code, bucket);
        return code;
    }

    @Override
    public String getTicket(String code) {
        String key = String.format("%s%s", Constants.CODE_CACHE_PREFIX, code);
        RBucket<String> bucket = redisClient.getRedissonClient().getBucket(key);
        return bucket.get();
    }

    @Override
    public boolean removeCode(String code) {
        String key = String.format("%s%s", Constants.CODE_CACHE_PREFIX, code);
        RBucket<String> bucket = redisClient.getRedissonClient().getBucket(key);
        return bucket.delete();
    }

    /**
     * 删除本地缓存
     *
     * @param codeKey redis code key
     */
    private void removeCache(String codeKey) {
        int index = codeKey.lastIndexOf("_");
        String code = index == -1 ? codeKey : codeKey.substring(index + 1);
        cache.invalidate(code);
    }

    /**
     * 缓存移除回调函数
     *
     * @param notification 移除通知
     */
    private void onRemoval(RemovalNotification<String, RBucket<String>> notification) {
        RemovalCause cause = notification.getCause();
        logger.info("code[{}] is removed from local cache, cause[{}]", notification.getKey(), cause.name());
    }
}
