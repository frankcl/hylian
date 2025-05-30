package xin.manong.hylian.server.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.hylian.server.util.AppSecretUtils;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.service.CaptchaService;

import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 *
 * @author frankcl
 * @date 2024-09-21 17:14:30
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final Logger logger = LoggerFactory.getLogger(CaptchaServiceImpl.class);

    private static final int CODE_LENGTH = 4;

    protected Cache<String, String> codeCache;

    public CaptchaServiceImpl() {
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
        RemovalCause cause = notification.getCause();
        logger.debug("Captcha:{} is removed for reason:{}", notification.getValue(), cause.name());
    }
}
