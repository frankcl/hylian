package xin.manong.hylian.client.aspect;

import org.springframework.context.annotation.Import;
import xin.manong.hylian.client.component.UserServiceSupport;
import xin.manong.hylian.client.config.HylianClientConfig;

import java.lang.annotation.*;

/**
 * 启动访问控制切面
 *
 * @author frankcl
 * @date 2023-10-13 17:47:37
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Import({ HylianClientConfig.class, ACLAspect.class, UserServiceSupport.class })
public @interface EnableACLAspect {
}
