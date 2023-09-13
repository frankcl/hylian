package xin.manong.security.keeper.sso.client.filter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.security.keeper.sso.client.common.Constants;
import xin.manong.security.keeper.sso.client.core.SecurityChecker;

import javax.servlet.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 单点登录过滤器抽象实现
 *
 * @author frankcl
 * @date 2023-09-04 14:34:57
 */
public abstract class SecurityFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    protected String name;
    protected String appId;
    protected String appSecret;
    protected String serverURL;
    protected List<Pattern> excludePatterns;
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
        String excludeURLPatterns = filterConfig.getInitParameter(Constants.PARAM_EXCLUDE_PATTERNS);
        if (!StringUtils.isEmpty(excludeURLPatterns)) {
            excludePatterns = new ArrayList<>();
            String[] patterns = excludeURLPatterns.split(",");
            for (String pattern : patterns) {
                pattern = pattern.trim();
                if (StringUtils.isEmpty(pattern)) continue;
                excludePatterns.add(Pattern.compile(pattern));
            }
        }
        if (!serverURL.endsWith("/")) serverURL += "/";
        securityChecker = new SecurityChecker(appId, appSecret, serverURL);
        logger.info("filter[{}] init success", filterConfig.getFilterName());
    }

    @Override
    public void destroy() {
        logger.info("filter[{}] is destroying ...", name);
        logger.info("filter[{}] has been destroyed", name);
    }
}
