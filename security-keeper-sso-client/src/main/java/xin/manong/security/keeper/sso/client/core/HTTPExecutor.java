package xin.manong.security.keeper.sso.client.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.spring.web.WebResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * HTTP执行器
 *
 * @author frankcl
 * @date 2023-09-14 13:41:14
 */
public class HTTPExecutor {

    private static final Logger logger = LoggerFactory.getLogger(HTTPExecutor.class);

    private static final int HTTP_CODE_OK = 200;

    private static final HttpClient httpClient = new HttpClient();

    /**
     * 执行HTTP请求
     *
     * @param httpRequest HTTP请求
     * @param responseType 响应数据类型
     * @return 成功返回结果，否则返回null
     */
    public static <T> WebResponse<T> execute(HttpRequest httpRequest, Class<T> responseType, Class<?> ... paramTypes) {
        try (Response httpResponse = httpClient.execute(httpRequest)) {
            if (httpResponse == null || !httpResponse.isSuccessful() || httpResponse.code() != HTTP_CODE_OK) {
                logger.error("execute http request failed for url[{}], http code[{}]",
                        httpRequest.requestURL, httpResponse == null ? -1 : httpResponse.code());
                return null;
            }
            assert httpResponse.body() != null;
            String body = httpResponse.body().string();
            List<Class<?>> types = new ArrayList<>();
            types.add(responseType);
            if (paramTypes != null) Collections.addAll(types, paramTypes);
            TypeReference<WebResponse<T>> typeReference = new TypeReference<WebResponse<T>>(
                    types.toArray(new Class[0])) {};
            return JSON.parseObject(body, typeReference);
        } catch (Exception e) {
            logger.error("exception occurred for executing url[{}]", httpRequest.requestURL);
            logger.error(e.getMessage(), e);
            return null;
        }
    }


    /**
     * 执行HTTP请求并解构结果
     *
     * @param httpRequest HTTP请求
     * @param responseType 响应数据类型
     * @return 成功返回结果，否则返回null
     */
    public static <T> T executeAndUnwrap(HttpRequest httpRequest, Class<T> responseType, Class<?> ... paramTypes) {
        WebResponse<T> response = execute(httpRequest, responseType, paramTypes);
        if (response == null) return null;
        if (!response.status) {
            logger.error("http response failed for url[{}], message[{}]", httpRequest.requestURL, response.message);
            return null;
        }
        return response.data;
    }
}
