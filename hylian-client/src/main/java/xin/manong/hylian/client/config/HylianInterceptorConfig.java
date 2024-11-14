package xin.manong.hylian.client.config;

import jakarta.annotation.Resource;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.hylian.client.interceptor.HylianInterceptor;

/**
 * 安全拦截器配置
 *
 * @author frankcl
 * @date 2023-09-15 11:01:49
 */
@Data
@Configuration
public class HylianInterceptorConfig {

    private static final Logger logger = LoggerFactory.getLogger(HylianInterceptorConfig.class);

    @Resource
    protected HylianClientConfig clientConfig;

    /**
     * 构建Hylian拦截器
     *
     * @return Hylian拦截器
     */
    @Bean
    public HylianInterceptor buildHylianInterceptor() {
        if (clientConfig == null) {
            logger.error("client config is null");
            throw new IllegalArgumentException("客户端配置为空");
        }
        return new HylianInterceptor(clientConfig);
    }
}
