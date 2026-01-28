package xin.manong.hylian.client.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.hylian.client.core.HylianClient;

/**
 * Hylian客户端配置
 *
 * @author frankcl
 * @date 2023-09-05 16:25:10
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "hylian.client")
public class HylianClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(HylianClientConfig.class);

    public String appId;
    public String appSecret;
    public String serverURL;

    /**
     * 构建客户端
     *
     * @param clientConfig 客户端配置
     * @return 客户端
     */
    @Bean
    public HylianClient buildHylianClient(HylianClientConfig clientConfig) {
        return new HylianClient(clientConfig);
    }

    /**
     * 检测应用客户端配置有效性
     */
    public void check() {
        if (StringUtils.isEmpty(appId)) {
            logger.error("App id is empty");
            throw new IllegalArgumentException("应用ID为空");
        }
        if (StringUtils.isEmpty(appSecret)) {
            logger.error("App secret is empty");
            throw new IllegalArgumentException("应用秘钥为空");
        }
        if (StringUtils.isEmpty(serverURL)) {
            logger.error("Server url is empty");
            throw new IllegalArgumentException("安全检测服务URL为空");
        }
        if (!serverURL.endsWith("/")) serverURL += "/";
    }
}
