package xin.manong.security.keeper.sso.client.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 应用客户端配置
 *
 * @author frankcl
 * @date 2023-09-05 16:25:10
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.security.sso")
public class AppClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppClientConfig.class);

    public String appId;
    public String appSecret;
    public String serverURL;

    /**
     * 检测应用客户端配置有效性
     */
    public void check() {
        if (StringUtils.isEmpty(appId)) {
            logger.error("app id is empty");
            throw new RuntimeException("应用ID为空");
        }
        if (StringUtils.isEmpty(appSecret)) {
            logger.error("app secret is empty");
            throw new RuntimeException("应用秘钥为空");
        }
        if (StringUtils.isEmpty(serverURL)) {
            logger.error("server url is empty");
            throw new RuntimeException("安全检测服务URL为空");
        }
    }
}
