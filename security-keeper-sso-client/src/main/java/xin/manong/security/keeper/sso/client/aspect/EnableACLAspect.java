package xin.manong.security.keeper.sso.client.aspect;

import java.lang.annotation.*;

/**
 * 启动访问控制切面
 *
 * @author frankcl
 * @date 2023-10-13 17:47:37
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface EnableACLAspect {
}
