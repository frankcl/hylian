package xin.manong.security.keeper.sso.client.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.security.keeper.sso.client.common.Constants;
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
@ConfigurationProperties(prefix = "app.security.sso")
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
        bean.addInitParameter(Constants.PARAM_APP_ID, appId);
        bean.addInitParameter(Constants.PARAM_APP_SECRET, appSecret);
        bean.addInitParameter(Constants.PARAM_SERVER_URL, serverURL);
        if (excludePatterns != null && !excludePatterns.isEmpty()) {
            List<String> patterns = new ArrayList<>();
            for (String excludePattern : excludePatterns) patterns.add(checkExcludePattern(excludePattern));
            bean.addInitParameter(Constants.PARAM_EXCLUDE_PATTERNS, String.join(",", patterns));
        }
    }

    /**
     * 检测排除URL模式
     *
     * @param excludePattern 排除URL模式
     * @return 通过检测返回排除URL正则表达式，否则抛出异常
     */
    private String checkExcludePattern(String excludePattern) {
        if (StringUtils.isEmpty(excludePattern)) {
            logger.error("exclude pattern is empty");
            throw new RuntimeException("排除URL模式为空");
        }
        int pos = excludePattern.indexOf("*");
        if (pos == -1) return excludePattern.startsWith("/") ? excludePattern : String.format("/%s", excludePattern);
        int n = 0;
        for (int i = 0; i < excludePattern.length(); i++) {
            if (excludePattern.charAt(i) == '*') n++;
        }
        if (n == 1 && (pos == 0 || (pos == excludePattern.length() - 1 &&
                excludePattern.charAt(excludePattern.length() - 2) == '/'))) {
            String pattern = String.format("%s.%s", excludePattern.substring(0, pos),
                    excludePattern.substring(pos));
            return pos == 0 || pattern.startsWith("/") ? pattern : String.format("/%s", pattern);
        }
        logger.error("invalid exclude pattern[{}]", excludePattern);
        throw new RuntimeException(String.format("非法排除URL模式[%s]", excludePattern));
    }
}
