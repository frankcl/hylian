package xin.manong.security.keeper.sso.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.spring.web.WebResponse;

import javax.servlet.*;

/**
 * 单点登录过滤器抽象实现
 *
 * @author frankcl
 * @date 2023-09-04 14:34:57
 */
public abstract class SecurityFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    public static final String PARAM_APP_ID = "app_id";
    public static final String PARAM_APP_SECRET = "app_secret";
    public static final String PARAM_SERVER_URL = "server_url";

    protected String name;
    protected String appId;
    protected String appSecret;
    protected String serverURL;

    protected HttpClient httpClient;

    public SecurityFilter() {
        httpClient = new HttpClient();
    }

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

    /**
     * 执行HTTP请求
     *
     * @param httpRequest HTTP请求
     * @param typeReference 结果数据类型
     * @return 成功返回结果，否则返回null
     * @param <T>
     */
    protected  <T> WebResponse<T> execute(HttpRequest httpRequest, TypeReference<WebResponse<T>> typeReference) {
        Response httpResponse = httpClient.execute(httpRequest);
        try {
            if (httpResponse == null || !httpResponse.isSuccessful() || httpResponse.code() != 200) {
                logger.error("request failed for url[{}]", httpRequest.requestURL);
                return null;
            }
            WebResponse<T> response;
            if (typeReference == null) response = JSON.parseObject(httpResponse.body().string(), WebResponse.class);
            else response = JSON.parseObject(httpResponse.body().string(), typeReference);
            if (!response.status) {
                logger.error("request failed for url[{}], message[{}]", httpRequest.requestURL, response.message);
                return null;
            }
            return response;
        } catch (Exception e) {
            logger.error("request exception for url[{}], cause[{}]", httpRequest.requestURL, e.getMessage());
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (httpResponse != null) httpResponse.close();
        }
    }
}
