package xin.manong.hylian.server.aspect;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启动应用注入切面
 *
 * @author frankcl
 * @date 2024-10-13 17:47:37
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Import({ AppInjectAspect.class })
public @interface EnableAppInjectAspect {
}
