package xin.manong.security.keeper.sso.client.config;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.security.keeper.sso.client.common.Constants;
import xin.manong.security.keeper.sso.client.common.URLPattern;
import xin.manong.security.keeper.sso.client.filter.SecurityFilter;
import xin.manong.weapon.base.util.CommonUtil;

import javax.annotation.Resource;
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

    private static final int DEFAULT_FILTER_ORDER = -1000;

    @Resource
    public AppClientConfig appClientConfig;
    public int filterOrder = DEFAULT_FILTER_ORDER;
    public List<String> includePatterns;
    public List<String> excludePatterns;

    /**
     * 构建安全过滤器bean
     *
     * @return 安全过滤器bean
     */
    @Bean
    public FilterRegistrationBean<SecurityFilter> buildSecurityFilter() {
        if (appClientConfig == null) {
            logger.error("app client config is null");
            throw new RuntimeException("应用客户端配置为空");
        }
        appClientConfig.check();
        FilterRegistrationBean<SecurityFilter> bean = new FilterRegistrationBean<>();
        initFilterParameters(bean);
        if (includePatterns == null) includePatterns = new ArrayList<>();
        if (includePatterns.isEmpty()) includePatterns.add("/*");
        bean.setUrlPatterns(includePatterns);
        bean.setFilter(new SecurityFilter());
        bean.setName(SecurityFilter.class.getSimpleName());
        bean.setOrder(filterOrder);
        return bean;
    }

    /**
     * 初始化过滤器参数
     *
     * @param bean 安全过滤器bean
     */
    private void initFilterParameters(FilterRegistrationBean bean) {
        bean.addInitParameter(Constants.PARAM_APP_ID, appClientConfig.appId);
        bean.addInitParameter(Constants.PARAM_APP_SECRET, appClientConfig.appSecret);
        bean.addInitParameter(Constants.PARAM_SERVER_URL, appClientConfig.serverURL);
        if (excludePatterns != null && !excludePatterns.isEmpty()) {
            List<URLPattern> urlPatterns = new ArrayList<>();
            for (String excludePattern : excludePatterns) urlPatterns.add(processExcludePattern(excludePattern));
            bean.addInitParameter(Constants.PARAM_EXCLUDE_PATTERNS, JSON.toJSONString(urlPatterns));
        }
    }

    /**
     * 处理URL排除模式
     * 1. 检测pattern有效性
     * 2. 生成正则表达式pattern
     *
     * @param pattern URL排除模式
     * @return 成功返回URL正则表达式，否则抛出异常
     */
    private URLPattern processExcludePattern(String pattern) {
        if (StringUtils.isEmpty(pattern)) {
            logger.error("exclude pattern is empty");
            throw new RuntimeException("URL排除模式为空");
        }
        int p = pattern.indexOf("*");
        if (p == -1) return URLPattern.buildNormal(pattern.startsWith("/") ?
                pattern : String.format("/%s", pattern));
        int n = CommonUtil.characterOccurrence(pattern, '*');
        int len = pattern.length();
        if (n == 1 && len > 1 && (p == 0 || (p == len - 1 && pattern.charAt(len - 2) == '/'))) {
            String tempPattern = String.format("%s.%s", pattern.substring(0, p), pattern.substring(p));
            return URLPattern.buildRegex(p == 0 ? tempPattern : (tempPattern.startsWith("/") ?
                    tempPattern : String.format("/%s", tempPattern)));
        }
        logger.error("invalid exclude pattern[{}]", pattern);
        throw new RuntimeException(String.format("非法URL排除模式[%s]", pattern));
    }
}
