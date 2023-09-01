package xin.manong.security.keeper.config;

import lombok.Data;

/**
 * 安全码配置
 *
 * @author frankcl
 * @date 2023-09-01 16:34:43
 */
@Data
public class SecurityCodeConfig {

    private static final int DEFAULT_CACHE_CAPACITY = 1000;
    private static final int DEFAULT_EXPIRED_TIME_SECONDS = 180;

    public int cacheCapacity = DEFAULT_CACHE_CAPACITY;
    public int expiredTimeSeconds = DEFAULT_EXPIRED_TIME_SECONDS;
}
