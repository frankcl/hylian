package xin.manong.hylian.client.config;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.hylian.client.filter.CORSFilter;

/**
 * CORSFilter配置
 *
 * @author frankcl
 * @date 2024-09-05 16:25:10
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "hylian.filter.cors")
public class CORSFilterConfig {

    private static final Logger logger = LoggerFactory.getLogger(CORSFilterConfig.class);

    public int order = -2000;

    /**
     * 构建CORSFilter
     *
     * @return CORSFilter
     */
    @Bean
    public FilterRegistrationBean<CORSFilter> buildCORSFilter() {
        FilterRegistrationBean<CORSFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new CORSFilter());
        bean.setName(CORSFilter.class.getSimpleName());
        bean.setOrder(order);
        return bean;
    }
}
