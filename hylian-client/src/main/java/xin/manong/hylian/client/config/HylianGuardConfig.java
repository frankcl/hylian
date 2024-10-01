package xin.manong.hylian.client.config;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.hylian.client.common.Constants;
import xin.manong.hylian.client.common.URLPattern;
import xin.manong.hylian.client.filter.HylianGuard;
import xin.manong.weapon.base.util.CommonUtil;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Hylian警卫配置
 *
 * @author frankcl
 * @date 2023-09-05 16:25:10
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "hylian.filter.guard")
public class HylianGuardConfig {

    private static final Logger logger = LoggerFactory.getLogger(HylianGuardConfig.class);

    @Resource
    public HylianClientConfig clientConfig;

    public int order = -1000;
    public List<String> includePatterns;
    public List<String> excludePatterns;

    /**
     * 构建Hylian警卫
     *
     * @return Hylian警卫
     */
    @Bean
    public FilterRegistrationBean<HylianGuard> buildHylianGuard() {
        clientConfig.check();
        FilterRegistrationBean<HylianGuard> bean = new FilterRegistrationBean<>();
        initHylianGuard(bean);
        if (includePatterns == null) includePatterns = new ArrayList<>();
        if (includePatterns.isEmpty()) includePatterns.add("/*");
        bean.setUrlPatterns(includePatterns);
        bean.setFilter(new HylianGuard());
        bean.setName(HylianGuard.class.getSimpleName());
        bean.setOrder(order);
        return bean;
    }

    /**
     * 初始化警卫参数
     *
     * @param bean Hylian警卫
     */
    private void initHylianGuard(FilterRegistrationBean<HylianGuard> bean) {
        bean.addInitParameter(Constants.PARAM_APP_ID, clientConfig.appId);
        bean.addInitParameter(Constants.PARAM_APP_SECRET, clientConfig.appSecret);
        bean.addInitParameter(Constants.PARAM_SERVER_URL, clientConfig.serverURL);
        if (excludePatterns != null && !excludePatterns.isEmpty()) {
            List<URLPattern> patterns = new ArrayList<>();
            for (String excludePattern : excludePatterns) patterns.add(processExcludePattern(excludePattern));
            bean.addInitParameter(Constants.PARAM_EXCLUDE_PATTERNS, JSON.toJSONString(patterns));
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
        throw new IllegalStateException(String.format("非法URL排除模式[%s]", pattern));
    }
}
