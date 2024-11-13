package xin.manong.hylian.client.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.jersey.WebResponse;

import java.util.List;
import java.util.Map;

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
     * 检测HTTP响应是否失败
     *
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @return 失败返回true，否则返回false
     */
    private static boolean failHttpResponse(HttpRequest httpRequest, Response httpResponse) {
        if (httpResponse == null || !httpResponse.isSuccessful() || httpResponse.code() != HTTP_CODE_OK) {
            logger.error("execute http request failed for url[{}], http code[{}]",
                    httpRequest.requestURL, httpResponse == null ? -1 : httpResponse.code());
            return true;
        }
        return false;
    }

    /**
     * 执行HTTP请求，返回字节数组
     *
     * @param httpRequest HTTP请求
     * @return 成功返回相应字节数组，否则返回null
     */
    public static byte[] executeRaw(HttpRequest httpRequest) {
        try (Response httpResponse = httpClient.execute(httpRequest)) {
            if (failHttpResponse(httpRequest, httpResponse)) return null;
            assert httpResponse.body() != null;
            return httpResponse.body().bytes();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 执行HTTP请求
     *
     * @param httpRequest HTTP请求
     * @return 成功返回结果，否则返回null
     */
    public static String execute(HttpRequest httpRequest) {
        try (Response httpResponse = httpClient.execute(httpRequest)) {
            if (failHttpResponse(httpRequest, httpResponse)) return null;
            assert httpResponse.body() != null;
            return httpResponse.body().string();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 执行HTTP请求，返回结构化结果
     *
     * @param httpRequest HTTP请求
     * @param recordType 数据类型
     * @return 成功返回结构化结果，否则返回null
     */
    public static <T> WebResponse<T> execute(HttpRequest httpRequest, Class<T> recordType) {
        String body = execute(httpRequest);
        if (body == null) return null;
        try {
            return JSON.parseObject(body, new TypeReference<WebResponse<T>>(recordType) {});
        } catch (Exception e) {
            logger.error("unexpected http response[{}] for {}", body, recordType.getName());
            return null;
        }
    }

    /**
     * 执行HTTP请求，返回列表结构化结果
     *
     * @param httpRequest HTTP请求
     * @param recordType 列表数据类型
     * @return 成功返回列表结构化结果，否则返回null
     * @param <T> 列表数据类型
     */
    public static <T> WebResponse<List<T>> executeList(HttpRequest httpRequest, Class<T> recordType) {
        String body = execute(httpRequest);
        if (body == null) return null;
        try {
            return JSON.parseObject(body, new TypeReference<WebResponse<List<T>>>(recordType) {});
        } catch (Exception e) {
            logger.error("unexpected http response[{}] for {} of List", body, recordType.getName());
            return null;
        }
    }

    /**
     * 执行HTTP请求，返回Map结构化结果
     *
     * @param httpRequest HTTP请求
     * @param keyType Map键类型
     * @param valueType Map值类型
     * @return 成功返回Map结构化结果，否则返回null
     * @param <K> Map键类型
     * @param <V> Map值类型
     */
    public static <K, V> WebResponse<Map<K, V>> executeMap(HttpRequest httpRequest,
                                                           Class<K> keyType, Class<V> valueType) {
        String body = execute(httpRequest);
        if (body == null) return null;
        try {
            return JSON.parseObject(body, new TypeReference<WebResponse<Map<K, V>>>(keyType, valueType) {});
        } catch (Exception e) {
            logger.error("unexpected http response[{}] for {} and {} of Map",
                    body, keyType.getName(), valueType.getName());
            return null;
        }
    }


    /**
     * 执行HTTP请求并解构结果
     *
     * @param httpRequest HTTP请求
     * @param recordType 响应数据类型
     * @return 成功返回结果，否则返回null
     */
    public static <T> T executeAndUnwrap(HttpRequest httpRequest, Class<T> recordType) {
        WebResponse<T> response = execute(httpRequest, recordType);
        if (response == null) return null;
        if (!response.status) {
            logger.error("execute and unwrap failed for url[{}], message[{}]",
                    httpRequest.requestURL, response.message);
            return null;
        }
        return response.data;
    }

    /**
     * 执行HTTP请求并解构列表结果
     *
     * @param httpRequest HTTP请求
     * @param recordType 列表成员数据类型
     * @return 成功返回结果，否则返回null
     */
    public static <T> List<T> executeAndUnwrapList(HttpRequest httpRequest, Class<T> recordType) {
        WebResponse<List<T>> response = executeList(httpRequest, recordType);
        if (response == null) return null;
        if (!response.status) {
            logger.error("execute and unwrap list failed for url[{}], message[{}]",
                    httpRequest.requestURL, response.message);
            return null;
        }
        return response.data;
    }

    /**
     * 执行HTTP请求并解构Map结果
     *
     * @param httpRequest HTTP请求
     * @param keyType Map键数据类型
     * @param valueType Map值数据类型
     * @return 成功返回结果，否则返回null
     */
    public static <K, V> Map<K, V> executeAndUnwrapMap(HttpRequest httpRequest, Class<K> keyType, Class<V> valueType) {
        WebResponse<Map<K, V>> response = executeMap(httpRequest, keyType, valueType);
        if (response == null) return null;
        if (!response.status) {
            logger.error("execute and unwrap map failed for url[{}], message[{}]",
                    httpRequest.requestURL, response.message);
            return null;
        }
        return response.data;
    }
}
