package xin.manong.security.keeper.sso.client.config;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.security.keeper.sso.client.core.SessionListener;

import javax.annotation.Resource;

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
    protected AppClientConfig appClientConfig;

    /**
     * 构建session监听器
     *
     * @return session监听器bean
     */
    @Bean
    public ServletListenerRegistrationBean<SessionListener> buildSessionListener() {
        if (appClientConfig == null) {
            logger.error("app client config is null");
            throw new RuntimeException("应用客户端配置为空");
        }
        appClientConfig.check();
        ServletListenerRegistrationBean<SessionListener> bean = new ServletListenerRegistrationBean();
        bean.setListener(new SessionListener(appClientConfig));
        return bean;
    }
}
