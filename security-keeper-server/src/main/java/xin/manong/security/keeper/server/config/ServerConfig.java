package xin.manong.security.keeper.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xin.manong.security.keeper.server.monitor.SweepMonitor;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpClientConfig;
import xin.manong.weapon.base.log.JSONLogger;

/**
 * @author frankcl
 * @date 2023-08-31 17:34:10
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.server")
public class ServerConfig implements WebMvcConfigurer {

    public String name;
    public String aspectLogFile;
    public JWTConfig jwtConfig;

    @Bean(name = "webAspectLogger")
    public JSONLogger webAspectLogger() {
        return new JSONLogger(aspectLogFile, null);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public SweepMonitor buildSweepMonitor() {
        return new SweepMonitor();
    }

    @Bean
    public HttpClient buildHttpClient() {
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        return new HttpClient(httpClientConfig);
    }
}
