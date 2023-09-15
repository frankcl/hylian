package xin.manong.security.keeper.sso.client.config;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.security.keeper.sso.client.interceptor.SecurityInterceptor;

import javax.annotation.Resource;

/**
 * 安全拦截器配置
 *
 * @author frankcl
 * @date 2023-09-15 11:01:49
 */
@Data
@Configuration
public class SecurityInterceptorConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityInterceptorConfig.class);

    @Resource
    protected AppClientConfig appClientConfig;

    /**
     * 构建安全拦截器
     *
     * @return 安全拦截器
     */
    @Bean
    public SecurityInterceptor buildSecurityInterceptor() {
        if (appClientConfig == null) {
            logger.error("app client config is null");
            throw new RuntimeException("应用客户端配置为空");
        }
        return new SecurityInterceptor(appClientConfig);
    }
}
