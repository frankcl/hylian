package xin.manong.hylian.client.filter;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.hylian.client.common.Constants;
import xin.manong.hylian.client.common.URLPattern;
import xin.manong.hylian.client.core.HylianShield;
import xin.manong.hylian.client.core.ContextManager;
import xin.manong.hylian.client.util.HTTPUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Hylian警卫：安全登录检测
 * 通过配置注解EnableHylianGuard启动HylianGuard
 *
 * @author frankcl
 * @date 2023-09-04 14:34:57
 */
public class HylianGuard implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(HylianGuard.class);

    protected String appId;
    protected String appSecret;
    protected String serverURL;
    protected List<URLPattern> excludePatterns;
    protected HylianShield shield;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Hylian guard is init ...");
        appId = filterConfig.getInitParameter(Constants.PARAM_APP_ID);
        if (StringUtils.isEmpty(appId)) {
            logger.error("param[{}] is not found", Constants.PARAM_APP_ID);
            throw new IllegalArgumentException(String.format("参数缺失[%s]", Constants.PARAM_APP_ID));
        }
        appSecret = filterConfig.getInitParameter(Constants.PARAM_APP_SECRET);
        if (StringUtils.isEmpty(appSecret)) {
            logger.error("param[{}] is not found", Constants.PARAM_APP_SECRET);
            throw new IllegalArgumentException(String.format("参数缺失[%s]", Constants.PARAM_APP_SECRET));
        }
        serverURL = filterConfig.getInitParameter(Constants.PARAM_SERVER_URL);
        if (StringUtils.isEmpty(serverURL)) {
            logger.error("param[{}] is not found", Constants.PARAM_SERVER_URL);
            throw new IllegalArgumentException(String.format("参数缺失[%s]", Constants.PARAM_SERVER_URL));
        }
        if (!serverURL.endsWith("/")) serverURL += "/";
        buildExcludePatterns(filterConfig);
        shield = new HylianShield(appId, appSecret, serverURL);
        logger.info("Hylian guard init success");
    }

    @Override
    public void destroy() {
        logger.info("Hylian guard is destroying ...");
        logger.info("Hylian guard has been destroyed");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestPath = HTTPUtils.getRequestPath(httpRequest);
        if (matchExcludePath(requestPath) || shield.shelter(httpRequest, httpResponse)) {
            try {
                ContextManager.fillContext(httpRequest);
                chain.doFilter(request, response);
            } finally {
                ContextManager.sweepContext();
            }
        }
    }

    /**
     * 判断是否匹配排除路径
     *
     * @param requestPath 请求路径
     * @return 匹配返回true，否则返回false
     */
    private boolean matchExcludePath(String requestPath) {
        if (excludePatterns == null || excludePatterns.isEmpty()) return false;
        for (URLPattern pattern : excludePatterns) {
            if (pattern.regex && pattern.matcher.matcher(requestPath).matches()) return true;
            if (!pattern.regex && requestPath.equals(pattern.pattern)) return true;
        }
        return false;
    }

    /**
     * 构建URL排除模式
     *
     * @param filterConfig 过滤器配置
     */
    private void buildExcludePatterns(FilterConfig filterConfig) {
        String value = filterConfig.getInitParameter(Constants.PARAM_EXCLUDE_PATTERNS);
        if (StringUtils.isEmpty(value)) return;
        excludePatterns = JSON.parseArray(value, URLPattern.class);
        for (URLPattern pattern : excludePatterns) {
            if (pattern.regex) pattern.matcher = Pattern.compile(pattern.pattern);
        }
    }
}
