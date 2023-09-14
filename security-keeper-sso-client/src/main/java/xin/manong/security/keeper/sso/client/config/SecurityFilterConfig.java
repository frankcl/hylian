package xin.manong.security.keeper.sso.client.config;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.security.keeper.sso.client.common.Constants;
import xin.manong.security.keeper.sso.client.common.URLPattern;
import xin.manong.security.keeper.sso.client.core.SessionListener;
import xin.manong.security.keeper.sso.client.filter.SecurityFilter;

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

    public int filterOrder = DEFAULT_FILTER_ORDER;
    public String appId;
    public String appSecret;
    public String serverURL;
    public List<String> includePatterns;
    public List<String> excludePatterns;

    /**
     * 构建安全过滤器bean
     *
     * @return 安全过滤器bean
     */
    @Bean
    public FilterRegistrationBean<SecurityFilter> buildSecurityFilter() {
        checkInitParameters();
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
     * 构建session监听器
     *
     * @return session监听器bean
     */
    @Bean
    public ServletListenerRegistrationBean<SessionListener> buildSessionListener() {
        checkInitParameters();
        ServletListenerRegistrationBean<SessionListener> bean = new ServletListenerRegistrationBean();
        bean.setListener(new SessionListener(serverURL, appId, appSecret));
        return bean;
    }

    /**
     * 初始化过滤器参数
     *
     * @param bean 安全过滤器bean
     */
    private void initFilterParameters(FilterRegistrationBean bean) {
        bean.addInitParameter(Constants.PARAM_APP_ID, appId);
        bean.addInitParameter(Constants.PARAM_APP_SECRET, appSecret);
        bean.addInitParameter(Constants.PARAM_SERVER_URL, serverURL);
        if (excludePatterns != null && !excludePatterns.isEmpty()) {
            List<URLPattern> urlPatterns = new ArrayList<>();
            for (String excludePattern : excludePatterns) urlPatterns.add(processExcludePattern(excludePattern));
            bean.addInitParameter(Constants.PARAM_EXCLUDE_PATTERNS, JSON.toJSONString(urlPatterns));
        }
    }

    /**
     * 检测参数有效性，无效抛出异常
     */
    private void checkInitParameters() {
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
        if (p == -1) return URLPattern.buildNormal(completeExcludePattern(pattern));
        int n = computeCharNum(pattern, '*');
        int len = pattern.length();
        if (n == 1 && len > 1 && (p == 0 || (p == len - 1 && pattern.charAt(len - 2) == '/'))) {
            String tempPattern = String.format("%s.%s", pattern.substring(0, p), pattern.substring(p));
            return URLPattern.buildRegex(p == 0 ? tempPattern : completeExcludePattern(tempPattern));
        }
        logger.error("invalid exclude pattern[{}]", pattern);
        throw new RuntimeException(String.format("非法URL排除模式[%s]", pattern));
    }

    /**
     * 补全URL排除模式
     *
     * @param pattern URL排除模式
     * @return 完整URL排除模式
     */
    private String completeExcludePattern(String pattern) {
        return pattern.startsWith("/") ? pattern : String.format("/%s", pattern);
    }

    /**
     * 计算字符串中包含字符个数
     *
     * @param s 字符串
     * @param c 字符
     * @return 字符个数
     */
    private int computeCharNum(String s, char c) {
        int n = 0;
        if (StringUtils.isEmpty(s)) return n;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == c) n++;
        return n;
    }
}
