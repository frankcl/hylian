package xin.manong.security.keeper.sso.client.annotation;

import org.springframework.context.annotation.Import;
import xin.manong.security.keeper.sso.client.component.UserRolePermissionService;
import xin.manong.security.keeper.sso.client.config.AppClientConfig;
import xin.manong.security.keeper.sso.client.config.SecurityFilterConfig;
import xin.manong.security.keeper.sso.client.config.SessionListenerConfig;

import java.lang.annotation.*;

/**
 * 安全过滤器启动注解
 * 需要使用登录检测的应用通过该注解启动servlet安全过滤器
 *
 * @author frankcl
 * @date 2023-09-15 10:34:37
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({ AppClientConfig.class, SecurityFilterConfig.class, SessionListenerConfig.class, UserRolePermissionService.class })
public @interface EnableSecurityFilter {
}
