package xin.manong.hylian.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.PropertySource;
import xin.manong.hylian.client.annotation.EnableCORSFilter;
import xin.manong.hylian.client.annotation.EnableHylianGuard;
import xin.manong.hylian.client.aspect.EnableACLAspect;
import xin.manong.weapon.spring.boot.annotation.EnableEtcdClient;
import xin.manong.weapon.spring.boot.annotation.EnableOSSClient;
import xin.manong.weapon.spring.boot.annotation.EnableRedisClient;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;
import xin.manong.weapon.spring.boot.etcd.EnableWatchValueBeanProcessor;
import xin.manong.weapon.spring.boot.io.EtcdPropertySourceFactory;

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
@EnableWatchValueBeanProcessor
@EnableCORSFilter
@EnableHylianGuard
@EnableACLAspect
@EnableWebLogAspect
@ServletComponentScan
@PropertySource(name="default", value = "application-${spring.profiles.active}.yml", factory = EtcdPropertySourceFactory.class)
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
