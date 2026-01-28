package xin.manong.hylian.client.filter;

import com.alibaba.fastjson2.JSON;
import jakarta.ws.rs.NotAuthorizedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.hylian.client.common.Constants;
import xin.manong.hylian.client.common.URLPattern;
import xin.manong.hylian.client.core.CookieSweeper;
import xin.manong.hylian.client.core.HylianClient;
import xin.manong.hylian.client.core.HylianShield;
import xin.manong.hylian.client.core.ContextManager;
import xin.manong.hylian.client.util.HTTPUtils;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import xin.manong.hylian.client.util.SessionUtils;

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
public class HylianGuard extends CookieSweeper implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(HylianGuard.class);

    protected HylianClient client;
    protected List<URLPattern> excludePatterns;
    protected HylianShield shield;

    public HylianGuard(HylianClient client) {
        this.client = client;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Hylian guard is init ...");
        buildExcludePatterns(filterConfig);
        shield = new HylianShield(client);
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
                ContextManager.setUser(SessionUtils.getUser(httpRequest));
                chain.doFilter(request, response);
            } catch (NotAuthorizedException e) {
                sweepCookies(httpRequest, httpResponse);
                throw e;
            } finally {
                ContextManager.removeUser();
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
