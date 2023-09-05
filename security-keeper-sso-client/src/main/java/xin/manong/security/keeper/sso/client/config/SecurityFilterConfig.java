package xin.manong.security.keeper.sso.client.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.security.keeper.sso.client.filter.SecurityFilter;
import xin.manong.security.keeper.sso.client.filter.SecurityLoginFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全过滤器配置
 *
 * @author frankcl
 * @date 2023-09-05 16:25:10
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "security-keeper.sso")
public class SecurityFilterConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilterConfig.class);

    public String appId;
    public String appSecret;
    public String serverURL;
    public List<String> interceptPatterns;
    public List<String> excludePatterns;

    /**
     * 构建安全登录过滤器bean
     *
     * @return 安全登录过滤器bean
     */
    @Bean
    public FilterRegistrationBean<SecurityLoginFilter> buildSecurityLoginFilter() {
        SecurityLoginFilter filter = new SecurityLoginFilter();
        FilterRegistrationBean<SecurityLoginFilter> bean = new FilterRegistrationBean<>();
        addInitParameters(bean);
        if (interceptPatterns == null) interceptPatterns = new ArrayList<>();
        if (interceptPatterns.isEmpty()) interceptPatterns.add("/*");
        bean.setUrlPatterns(interceptPatterns);
        bean.setFilter(filter);
        bean.setName(filter.getClass().getSimpleName());
        bean.setOrder(1);
        return bean;
    }

    /**
     * 添加初始化参数
     *
     * @param bean 安全过滤器bean
     */
    private void addInitParameters(FilterRegistrationBean bean) {
        if (StringUtils.isEmpty(appId)) {
            logger.error("app id is empty");
            throw new RuntimeException("应用ID为空");
        }
        if (StringUtils.isEmpty(appSecret)) {
            logger.error("app secret is empty");
            throw new RuntimeException("应用秘钥为空");
        }
        if (StringUtils.isEmpty(serverURL)) {
            logger.error("server URL is empty");
            throw new RuntimeException("单点登录服务URL为空");
        }
        bean.addInitParameter(SecurityFilter.PARAM_APP_ID, appId);
        bean.addInitParameter(SecurityFilter.PARAM_APP_SECRET, appSecret);
        bean.addInitParameter(SecurityFilter.PARAM_SERVER_URL, serverURL);
        if (excludePatterns != null && !excludePatterns.isEmpty()) {
            bean.addInitParameter(SecurityFilter.PARAM_EXCLUDE_PATTERNS, String.join(",", excludePatterns));
        }
    }
}
