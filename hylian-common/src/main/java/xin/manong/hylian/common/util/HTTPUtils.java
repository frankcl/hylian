package xin.manong.hylian.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * HTTP工具
 *
 * @author frankcl
 * @date 2023-09-01 17:52:54
 */
public class HTTPUtils {

    private static final Logger logger = LoggerFactory.getLogger(HTTPUtils.class);

    private static final int DEFAULT_HTTP_PORT = 80;
    private static final int DEFAULT_HTTPS_PORT = 443;
    private static final String PROTOCOL_HTTP = "http";
    private static final String PROTOCOL_HTTPS = "https";
    private static final String HTTP_REQUEST_HEAD_ORIGIN = "Origin";
    private static final String HTTP_REQUEST_HEAD_ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
    private static final String HTTP_RESPONSE_HEAD_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String HTTP_RESPONSE_HEAD_ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    private static final String HTTP_RESPONSE_HEAD_ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    private static final String HTTP_RESPONSE_HEAD_ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

    /**
     * 获取请求URL
     *
     * @param httpRequest HTTP请求
     * @return 请求URL
     */
    public static String getRequestURL(HttpServletRequest httpRequest) {
        String requestURL = httpRequest.getRequestURL().toString();
        String query = httpRequest.getQueryString();
        return StringUtils.isEmpty(query) ? requestURL : String.format("%s?%s", requestURL, query);
    }

    /**
     * 获取请求根URL
     *
     * @param httpRequest HTTP请求
     * @return 请求根URL
     */
    public static String getRequestRootURL(HttpServletRequest httpRequest) {
        try {
            URL requestURL = new URL(httpRequest.getRequestURL().toString());
            String host = requestURL.getHost();
            String protocol = requestURL.getProtocol();
            int port = getPort(requestURL);
            return port == -1 ? String.format("%s://%s", protocol, host) :
                    String.format("%s://%s:%d", protocol, host, port);
        } catch (Exception e) {
            logger.error("get request base URL failed");
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取请求URL path
     *
     * @param httpRequest HTTP请求
     * @return 成功返回请求URL path，否则返回null
     */
    public static String getRequestPath(HttpServletRequest httpRequest) {
        try {
            URL requestURL = new URL(httpRequest.getRequestURL().toString());
            return requestURL.getPath();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取请求query字典
     *
     * @param httpRequest HTTP请求
     * @return query字典
     */
    public static Map<String, String> getRequestQueryMap(HttpServletRequest httpRequest) {
        Map<String, String> queryMap = new HashMap<>();
        String queryString = httpRequest.getQueryString();
        if (StringUtils.isEmpty(queryString)) return queryMap;
        String[] queryList = queryString.split("&");
        for (String query : queryList) {
            query = query.trim();
            int index = query.indexOf("=");
            if (index == -1) queryMap.put(query, null);
            else queryMap.put(query.substring(0, index).trim(), query.substring(index + 1).trim());
        }
        return queryMap;
    }

    /**
     * 移除URL中指定query
     *
     * @param url URL
     * @param queryKeys query key集合
     * @return 移除query后URL
     */
    public static String removeQueries(String url, Set<String> queryKeys) {
        try {
            URL requestURL = new URL(url);
            String queryString = requestURL.getQuery();
            if (StringUtils.isEmpty(queryString)) return url;
            StringBuilder builder = new StringBuilder();
            String[] queryList = queryString.split("&");
            for (String query : queryList) {
                int index = query.indexOf("=");
                String key = index == -1 ? query : query.substring(0, index);
                if (queryKeys.contains(key)) continue;
                if (builder.length() > 0) builder.append("&");
                builder.append(query);
            }
            String protocol = requestURL.getProtocol();
            int port = requestURL.getPort();
            String path = port < 0 ?
                    String.format("%s://%s%s", protocol, requestURL.getHost(), requestURL.getPath()) :
                    String.format("%s://%s:%d%s", protocol, requestURL.getHost(), port, requestURL.getPath());
            return builder.length() > 0 ? String.format("%s?%s", path, builder) : path;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return url;
        }
    }

    /**
     * 获取URL端口
     *
     * @param requestURL 请求URL
     * @return 端口号，协议默认端口返回-1
     */
    private static int getPort(URL requestURL) {
        String protocol = requestURL.getProtocol();
        int port = requestURL.getPort();
        if (StringUtils.isEmpty(protocol)) return port;
        if (protocol.equals(PROTOCOL_HTTP) && port == DEFAULT_HTTP_PORT) return -1;
        else if (protocol.equals(PROTOCOL_HTTPS) && port == DEFAULT_HTTPS_PORT) return -1;
        return port;
    }
}
