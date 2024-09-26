package xin.manong.hylian.client.annotation;

import org.springframework.context.annotation.Import;
import xin.manong.hylian.client.component.UserServiceSupport;
import xin.manong.hylian.client.config.HylianClientConfig;
import xin.manong.hylian.client.config.HylianInterceptorConfig;
import xin.manong.hylian.client.config.SessionListenerConfig;

import java.lang.annotation.*;

/**
 * Hylian截器启动注解
 * 需要启用安全登录检测的应用通过该注解启动spring拦截器
 *
 * @author frankcl
 * @date 2023-09-15 10:34:37
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({ HylianClientConfig.class, HylianInterceptorConfig.class, SessionListenerConfig.class, UserServiceSupport.class })
public @interface EnableHylianInterceptor {
}
