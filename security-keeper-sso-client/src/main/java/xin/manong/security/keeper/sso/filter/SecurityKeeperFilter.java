package xin.manong.security.keeper.sso.filter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;

/**
 * 单点登录过滤器抽象实现
 *
 * @author frankcl
 * @date 2023-09-04 14:34:57
 */
public abstract class SecurityKeeperFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityKeeperFilter.class);

    public static final String PARAM_APP_ID = "app_id";
    public static final String PARAM_APP_SECRET = "app_secret";
    public static final String PARAM_SERVER_URL = "server_url";

    protected String name;
    protected String appId;
    protected String appSecret;
    protected String serverURL;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("filter[{}] is init ...", filterConfig.getFilterName());
        name = filterConfig.getFilterName();
        appId = filterConfig.getInitParameter(PARAM_APP_ID);
        if (StringUtils.isEmpty(appId)) {
            logger.error("param[{}] is not found", PARAM_APP_ID);
            throw new RuntimeException(String.format("过滤器参数[%s]未找到", PARAM_APP_ID));
        }
        appSecret = filterConfig.getInitParameter(PARAM_APP_SECRET);
        if (StringUtils.isEmpty(appSecret)) {
            logger.error("param[{}] is not found", PARAM_APP_SECRET);
            throw new RuntimeException(String.format("过滤器参数[%s]未找到", PARAM_APP_SECRET));
        }
        serverURL = filterConfig.getInitParameter(PARAM_SERVER_URL);
        if (StringUtils.isEmpty(serverURL)) {
            logger.error("param[{}] is not found", PARAM_SERVER_URL);
            throw new RuntimeException(String.format("过滤器参数[%s]未找到", PARAM_SERVER_URL));
        }
        if (!serverURL.endsWith("/")) serverURL += "/";
        logger.info("filter[{}] init success", filterConfig.getFilterName());
    }

    @Override
    public void destroy() {
        logger.info("filter[{}] is destroying ...", name);
        logger.info("filter[{}] has been destroyed", name);
    }
}
