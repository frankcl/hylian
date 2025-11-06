package xin.manong.hylian.client.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 跨域资源共享过滤器
 * 设置HTTP响应header允许跨域资源访问
 * 通过配置注解EnableCORSFilter启动CORSFilter
 *
 * @author frankcl
 * @date 2024-10-01 13:05:41
 */
public class CORSFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(CORSFilter.class);

    private static final String HTTP_OPTIONS = "OPTIONS";

    private static final String HTTP_REQUEST_HEAD_ORIGIN = "Origin";
    private static final String HTTP_REQUEST_HEAD_ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";

    private static final String HTTP_RESPONSE_HEAD_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String HTTP_RESPONSE_HEAD_ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    private static final String HTTP_RESPONSE_HEAD_ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    private static final String HTTP_RESPONSE_HEAD_ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("CORSFilter start to init");
        logger.info("CORSFilter init success");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String origin = httpRequest.getHeader(HTTP_REQUEST_HEAD_ORIGIN);
        if (origin != null) httpResponse.addHeader(HTTP_RESPONSE_HEAD_ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        httpResponse.addHeader(HTTP_RESPONSE_HEAD_ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        if (httpRequest.getMethod().equals(HTTP_OPTIONS)) {
            String requestHeaders = httpRequest.getHeader(HTTP_REQUEST_HEAD_ACCESS_CONTROL_REQUEST_HEADERS);
            httpResponse.addHeader(HTTP_RESPONSE_HEAD_ACCESS_CONTROL_ALLOW_HEADERS,
                    requestHeaders != null ? requestHeaders : "*");
            httpResponse.addHeader(HTTP_RESPONSE_HEAD_ACCESS_CONTROL_ALLOW_METHODS,
                    "GET, POST, PUT, DELETE, OPTIONS");
            httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("CORSFilter start to destroy");
        logger.info("CORSFilter has been destroyed");
    }
}
