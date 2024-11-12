package xin.manong.hylian.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import xin.manong.hylian.client.annotation.EnableCORSFilter;
import xin.manong.hylian.client.annotation.EnableHylianGuard;
import xin.manong.hylian.client.aspect.EnableACLAspect;
import xin.manong.weapon.spring.boot.annotation.EnableEtcdClient;
import xin.manong.weapon.spring.boot.annotation.EnableOSSClient;
import xin.manong.weapon.spring.boot.annotation.EnableRedisClient;
import xin.manong.weapon.spring.boot.etcd.EnableWatchValuePostProcessor;
import xin.manong.weapon.spring.web.ws.aspect.EnableWebLogAspect;

/**
 * 应用程序入口
 * spring boot应用
 *
 * @author frankcl
 * @date 2022-08-24 12:58:39
 */
@EnableRedisClient
@EnableEtcdClient
@EnableOSSClient
@EnableWatchValuePostProcessor
@EnableCORSFilter
@EnableHylianGuard
@EnableACLAspect
@EnableWebLogAspect
@ServletComponentScan
@SpringBootApplication(scanBasePackages = { "xin.manong.hylian.server" })
public class Application {

    /**
     * 应用入口
     *
     * @param args 参数
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
