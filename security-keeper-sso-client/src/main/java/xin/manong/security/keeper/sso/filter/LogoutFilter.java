package xin.manong.security.keeper.sso.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 注销过滤器
 *
 * @author frankcl
 * @date 2023-09-04 14:27:45
 */
public class LogoutFilter extends SecurityKeeperFilter {

    private static final Logger logger = LoggerFactory.getLogger(LogoutFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession httpSession = httpRequest.getSession();
        if (httpSession != null) {
            httpSession.invalidate();
            logger.info("session is invalidated for logout ");
        }
        chain.doFilter(request, response);
    }
}
