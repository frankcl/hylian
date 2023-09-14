package xin.manong.security.keeper.sso.client.filter;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.security.keeper.common.util.HTTPUtils;
import xin.manong.security.keeper.sso.client.common.Constants;
import xin.manong.security.keeper.sso.client.common.URLPattern;
import xin.manong.security.keeper.sso.client.core.SecurityChecker;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 单点登录过滤器抽象实现
 *
 * @author frankcl
 * @date 2023-09-04 14:34:57
 */
public class SecurityFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    protected String name;
    protected String appId;
    protected String appSecret;
    protected String serverURL;
    protected List<URLPattern> excludePatterns;
    protected SecurityChecker securityChecker;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("filter[{}] is init ...", filterConfig.getFilterName());
        name = filterConfig.getFilterName();
        appId = filterConfig.getInitParameter(Constants.PARAM_APP_ID);
        if (StringUtils.isEmpty(appId)) {
            logger.error("param[{}] is not found", Constants.PARAM_APP_ID);
            throw new RuntimeException(String.format("过滤器参数[%s]未找到", Constants.PARAM_APP_ID));
        }
        appSecret = filterConfig.getInitParameter(Constants.PARAM_APP_SECRET);
        if (StringUtils.isEmpty(appSecret)) {
            logger.error("param[{}] is not found", Constants.PARAM_APP_SECRET);
            throw new RuntimeException(String.format("过滤器参数[%s]未找到", Constants.PARAM_APP_SECRET));
        }
        serverURL = filterConfig.getInitParameter(Constants.PARAM_SERVER_URL);
        if (StringUtils.isEmpty(serverURL)) {
            logger.error("param[{}] is not found", Constants.PARAM_SERVER_URL);
            throw new RuntimeException(String.format("过滤器参数[%s]未找到", Constants.PARAM_SERVER_URL));
        }
        if (!serverURL.endsWith("/")) serverURL += "/";
        buildExcludePatterns(filterConfig);
        securityChecker = new SecurityChecker(appId, appSecret, serverURL);
        logger.info("filter[{}] init success", filterConfig.getFilterName());
    }

    @Override
    public void destroy() {
        logger.info("filter[{}] is destroying ...", name);
        logger.info("filter[{}] has been destroyed", name);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestPath = HTTPUtils.getRequestPath(httpRequest);
        if (matchExcludePath(requestPath) || securityChecker.check(httpRequest, httpResponse)) {
            chain.doFilter(request, response);
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
