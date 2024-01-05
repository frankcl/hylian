package xin.manong.security.keeper.sso.client.annotation;

import org.springframework.context.annotation.Import;
import xin.manong.security.keeper.sso.client.component.UserServiceSupport;
import xin.manong.security.keeper.sso.client.config.AppClientConfig;
import xin.manong.security.keeper.sso.client.config.SecurityInterceptorConfig;
import xin.manong.security.keeper.sso.client.config.SessionListenerConfig;

import java.lang.annotation.*;

/**
 * 安全拦截器启动注解
 * 需要使用登录检测的应用通过该注解启动spring安全拦截器
 *
 * @author frankcl
 * @date 2023-09-15 10:34:37
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({ AppClientConfig.class, SecurityInterceptorConfig.class, SessionListenerConfig.class, UserServiceSupport.class })
public @interface EnableSecurityInterceptor {
}
