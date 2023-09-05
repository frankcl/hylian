package xin.manong.security.keeper.server;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import xin.manong.weapon.spring.boot.annotation.EnableRedisClient;

/**
 * 应用测试入口
 *
 * @author frankcl
 * @date 2022-08-15 21:08:20
 */
@EnableRedisClient
@SpringBootApplication(scanBasePackages = { "xin.manong.security.keeper.server" })
public class ApplicationTest {
}
