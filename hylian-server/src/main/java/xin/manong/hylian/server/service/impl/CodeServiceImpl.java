package xin.manong.hylian.server.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import org.redisson.api.DeletedObjectListener;
import org.redisson.api.ExpiredObjectListener;
import org.redisson.api.RBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.service.CodeService;
import xin.manong.weapon.base.redis.RedisClient;
import xin.manong.weapon.base.util.RandomID;
import xin.manong.weapon.base.util.ShortKeyBuilder;

import javax.annotation.Resource;
import java.time.Duration;
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
    /**
     * 本地cache可以去掉，redis可以解决服务端多实例问题
     */
    protected Cache<String, RBucket<String>> codeCache;

    public CodeServiceImpl() {
        CacheBuilder<String, RBucket<String>> builder = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .maximumSize(Constants.LOCAL_CACHE_CAPACITY_CODE)
                .expireAfterAccess(Constants.CACHE_CODE_EXPIRED_TIME_MS, TimeUnit.MILLISECONDS)
                .removalListener(this::onRemoval);
        codeCache = builder.build();
    }

    @Override
    public String createCode(String ticket) {
        String code;
        RBucket<String> bucket;
        do {
            code = ShortKeyBuilder.build(RandomID.build());
            String key = String.format("%s%s", Constants.CODE_CACHE_PREFIX, code);
            bucket = redisClient.getRedissonClient().getBucket(key);
        } while (bucket.get() != null);
        bucket.addListener((ExpiredObjectListener) name -> {
            logger.info("code[{}] is expired", name);
            removeLocalCache(name);
        });
        bucket.addListener((DeletedObjectListener) name -> {
            logger.info("code[{}] is removed", name);
            removeLocalCache(name);
        });
        bucket.set(ticket, Duration.ofMillis(Constants.CACHE_CODE_EXPIRED_TIME_MS));
        codeCache.put(code, bucket);
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
        codeCache.invalidate(code);
        String key = String.format("%s%s", Constants.CODE_CACHE_PREFIX, code);
        RBucket<String> bucket = redisClient.getRedissonClient().getBucket(key);
        return bucket.delete();
    }

    /**
     * 删除本地缓存
     *
     * @param codeKey redis code key
     */
    private void removeLocalCache(String codeKey) {
        int index = codeKey.lastIndexOf("_");
        String code = index == -1 ? codeKey : codeKey.substring(index + 1);
        codeCache.invalidate(code);
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
