package xin.manong.security.keeper.sso.client.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.spring.web.WebResponse;

/**
 * HTTP执行器
 *
 * @author frankcl
 * @date 2023-09-14 13:41:14
 */
public class HTTPExecutor {

    private static final Logger logger = LoggerFactory.getLogger(HTTPExecutor.class);

    private static final int HTTP_CODE_OK = 200;

    private static HttpClient httpClient = new HttpClient();

    /**
     * 执行HTTP请求
     *
     * @param httpRequest HTTP请求
     * @param responseType 响应数据类型
     * @return 成功返回结果，否则返回null
     * @param <T>
     */
    public static <T> T execute(HttpRequest httpRequest, Class<T> responseType) {
        Response httpResponse = httpClient.execute(httpRequest);
        try {
            if (httpResponse == null || !httpResponse.isSuccessful() || httpResponse.code() != HTTP_CODE_OK) {
                logger.error("execute http request failed for url[{}], http code[{}]",
                        httpRequest.requestURL, httpResponse == null ? -1 : httpResponse.code());
                return null;
            }
            String body = httpResponse.body().string();
            TypeReference<WebResponse<T>> typeReference = new TypeReference<WebResponse<T>>(responseType) {};
            WebResponse<T> response = JSON.parseObject(body, typeReference);
            if (!response.status) {
                logger.error("http response failed for url[{}], message[{}]",
                        httpRequest.requestURL, response.message);
                return null;
            }
            return response.data;
        } catch (Exception e) {
            logger.error("exception occurred for executing http url[{}], cause[{}]",
                    httpRequest.requestURL, e.getMessage());
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (httpResponse != null) httpResponse.close();
        }
    }
}
