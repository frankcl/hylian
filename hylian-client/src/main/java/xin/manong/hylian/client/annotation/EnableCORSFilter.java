package xin.manong.hylian.client.annotation;

import org.springframework.context.annotation.Import;
import xin.manong.hylian.client.config.*;

import java.lang.annotation.*;

/**
 * CORSFilter启动注解
 * 需要开启跨域资源共享可启动servlet过滤器
 *
 * @author frankcl
 * @date 2024-09-15 10:34:37
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
@Import({ CORSFilterConfig.class })
public @interface EnableCORSFilter {
}
