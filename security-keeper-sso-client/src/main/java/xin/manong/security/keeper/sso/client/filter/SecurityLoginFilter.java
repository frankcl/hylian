package xin.manong.security.keeper.sso.client.filter;

import xin.manong.security.keeper.common.util.HTTPUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * 安全登录过滤器
 *
 * @author frankcl
 * @date 2023-09-04 14:26:59
 */
public class SecurityLoginFilter extends SecurityFilter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestPath = HTTPUtils.getRequestPath(httpRequest);
        if (matchExcludePatterns(requestPath) || securityChecker.check(httpRequest, httpResponse)) {
            chain.doFilter(request, response);
        }
    }

    /**
     * 判断是否匹配排除路径
     *
     * @param requestPath 请求路径
     * @return 匹配返回true，否则返回false
     */
    private boolean matchExcludePatterns(String requestPath) {
        if (excludePatterns == null || excludePatterns.isEmpty()) return false;
        for (String excludePattern : excludePatterns) {
            if (Pattern.matches(excludePattern, requestPath)) return true;
        }
        return false;
    }
}
