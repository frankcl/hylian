package xin.manong.hylian.client.config;

import jakarta.annotation.Resource;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.hylian.client.core.HylianClient;
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
    protected HylianClient client;

    /**
     * 构建Hylian拦截器
     *
     * @return Hylian拦截器
     */
    @Bean
    public HylianInterceptor buildHylianInterceptor() {
        if (client == null) {
            logger.error("Client is null");
            throw new IllegalArgumentException("客户端为空");
        }
        return new HylianInterceptor(client);
    }
}
