package xin.manong.hylian.client.annotation;

import org.springframework.context.annotation.Import;
import xin.manong.hylian.client.component.UserServiceSupport;
import xin.manong.hylian.client.config.HylianClientConfig;
import xin.manong.hylian.client.config.HylianGuardConfig;
import xin.manong.hylian.client.config.SessionListenerConfig;

import java.lang.annotation.*;

/**
 * Hylian守卫启动注解
 * 需要使用登录安全检测的应用通过该注解启动servlet过滤器
 *
 * @author frankcl
 * @date 2023-09-15 10:34:37
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
@Import({ HylianClientConfig.class, HylianGuardConfig.class,
        SessionListenerConfig.class, UserServiceSupport.class })
public @interface EnableHylianGuard {
}
