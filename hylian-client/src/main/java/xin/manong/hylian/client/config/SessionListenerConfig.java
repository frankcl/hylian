package xin.manong.hylian.client.config;

import jakarta.annotation.Resource;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.hylian.client.core.SessionListener;

/**
 * session监听器配置
 *
 * @author frankcl
 * @date 2023-09-15 10:46:44
 */
@Data
@Configuration
public class SessionListenerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SessionListenerConfig.class);

    @Resource
    protected HylianClientConfig hylianClientConfig;

    /**
     * 构建session监听器
     *
     * @return session监听器bean
     */
    @Bean
    public ServletListenerRegistrationBean<SessionListener> buildSessionListener() {
        if (hylianClientConfig == null) throw new IllegalArgumentException("客户端配置为空");
        hylianClientConfig.check();
        ServletListenerRegistrationBean<SessionListener> bean = new ServletListenerRegistrationBean<>();
        bean.setListener(new SessionListener(hylianClientConfig));
        return bean;
    }
}
