package xin.manong.hylian.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.hylian.server.monitor.Sweeper;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpClientConfig;
import xin.manong.weapon.base.log.JSONLogger;

/**
 * Hylian服务配置
 *
 * @author frankcl
 * @date 2023-08-31 17:34:10
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.server")
public class ServerConfig {

    public String name;
    public String aspectLogFile;
    public JWTConfig jwtConfig;

    @Bean(name = "webAspectLogger")
    public JSONLogger webAspectLogger() {
        return new JSONLogger(aspectLogFile, null);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Sweeper buildSweeper() {
        return new Sweeper();
    }

    @Bean
    public HttpClient buildHttpClient() {
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        return new HttpClient(httpClientConfig);
    }
}
