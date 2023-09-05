package xin.manong.security.keeper.sso.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.security.keeper.common.util.HTTPUtils;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.spring.web.WebResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 注销过滤器
 *
 * @author frankcl
 * @date 2023-09-04 14:27:45
 */
public class LogoutFilter extends SecurityFilter {

    private static final Logger logger = LoggerFactory.getLogger(LogoutFilter.class);

    private static final String CLIENT_PATH_LOGOUT = "/logout";
    private static final String CLIENT_PATH_EXECUTE_LOGOUT = "/sso/executeLogout";

    private static final String SERVER_PATH_SSO_LOGOUT = "sso/logout";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestPath = HTTPUtils.getRequestPath(httpRequest);
        if (requestPath.equals(CLIENT_PATH_LOGOUT)) logoutServer();
        else if (requestPath.equals(CLIENT_PATH_EXECUTE_LOGOUT)) {
            HttpSession httpSession = httpRequest.getSession();
            if (httpSession != null) {
                httpSession.invalidate();
                logger.info("session is invalidated for logout ");
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * SSO服务端注销
     */
    private void logoutServer() {
        String requestURL = String.format("%s%s", serverURL, SERVER_PATH_SSO_LOGOUT);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(PARAM_APP_ID, appId);
        paramMap.put(PARAM_APP_SECRET, appSecret);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        WebResponse response = execute(httpRequest, null);
        if (response == null) logger.warn("security keeper sso logout failed");
    }
}
