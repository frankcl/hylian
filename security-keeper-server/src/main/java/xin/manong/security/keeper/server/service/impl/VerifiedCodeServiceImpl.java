package xin.manong.security.keeper.server.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.security.keeper.common.util.AppSecretUtils;
import xin.manong.security.keeper.server.common.Constants;
import xin.manong.security.keeper.server.service.VerifiedCodeService;

import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 *
 * @author frankcl
 * @date 2024-09-21 17:14:30
 */
@Service
public class VerifiedCodeServiceImpl implements VerifiedCodeService {

    private static final Logger logger = LoggerFactory.getLogger(VerifiedCodeServiceImpl.class);

    private static final int CODE_LENGTH = 6;

    protected Cache<String, String> codeCache;

    public VerifiedCodeServiceImpl() {
        CacheBuilder<String, String> builder = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .maximumSize(Constants.LOCAL_CACHE_CAPACITY_CODE)
                .expireAfterAccess(Constants.CACHE_CODE_EXPIRED_TIME_MS, TimeUnit.MILLISECONDS)
                .removalListener(this::onRemoval);
        codeCache = builder.build();
    }

    @Override
    public String create(String sessionId) {
        String code = AppSecretUtils.buildSecret(CODE_LENGTH);
        codeCache.put(sessionId, code);
        return code;
    }

    @Override
    public String get(String sessionId) {
        String code = codeCache.getIfPresent(sessionId);
        if (StringUtils.isEmpty(code)) logger.warn("验证码过期");
        return code;
    }

    @Override
    public void remove(String sessionId) {
        codeCache.invalidate(sessionId);
    }

    /**
     * 验证码缓存移除回调
     *
     * @param notification 移除通知
     */
    private void onRemoval(RemovalNotification<String, String> notification) {
        logger.info("verify code[{}] is removed for reason[{}]", notification.getKey(), notification.getValue());
    }
}
