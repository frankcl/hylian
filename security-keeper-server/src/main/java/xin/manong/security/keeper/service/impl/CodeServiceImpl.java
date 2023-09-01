package xin.manong.security.keeper.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xin.manong.security.keeper.config.ServerConfig;
import xin.manong.security.keeper.service.CodeService;
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

    private Cache<String, String> cache;
    private ServerConfig serverConfig;

    @Autowired
    public CodeServiceImpl(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        CacheBuilder<String, String> builder = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .maximumSize(serverConfig.securityCodeConfig.cacheCapacity)
                .expireAfterAccess(serverConfig.securityCodeConfig.expiredTimeSeconds, TimeUnit.MINUTES)
                .removalListener(n -> onRemoval(n));
        cache = builder.build();
    }

    @Override
    public String createCode(String ticket) {
        String code = null;
        while (true) {
            code = ShortKeyBuilder.build(RandomID.build());
            if (StringUtils.isEmpty(code)) {
                logger.error("create code failed for ticket[{}]", ticket);
                throw new RuntimeException("创建安全码失败");
            }
            if (cache.getIfPresent(code) == null) break;
        }
        cache.put(code, ticket);
        return code;
    }

    @Override
    public String getTicket(String code) {
        return cache.getIfPresent(code);
    }

    @Override
    public boolean removeCode(String code) {
        String ticket = cache.getIfPresent(code);
        cache.invalidate(code);
        return ticket != null;
    }

    /**
     * 缓存移除回调函数
     *
     * @param notification 移除通知
     */
    private void onRemoval(RemovalNotification<String, String> notification) {
        RemovalCause cause = notification.getCause();
        logger.info("code[{}] is removed, cause[{}]", notification.getKey(), cause.name());
    }
}
