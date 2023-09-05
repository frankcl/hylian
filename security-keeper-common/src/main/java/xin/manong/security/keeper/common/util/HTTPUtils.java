package xin.manong.security.keeper.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.Set;

/**
 * HTTP工具
 *
 * @author frankcl
 * @date 2023-09-01 17:52:54
 */
public class HTTPUtils {

    private static final Logger logger = LoggerFactory.getLogger(HTTPUtils.class);

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
     * 获取请求base URL
     *
     * @param httpRequest HTTP请求
     * @return 请求base URL
     */
    public static String getRequestBaseURL(HttpServletRequest httpRequest) {
        try {
            String requestURL = httpRequest.getRequestURL().toString();
            URL url = new URL(requestURL);
            String schema = url.getProtocol();
            String host = url.getHost();
            int port = url.getPort();
            if (schema.equals("http") && port == 80) port = -1;
            else if (schema.equals("https") && port == 443) port = -1;
            return port == -1 ? String.format("%s://%s", schema, host) : String.format("%s://%s:%d", schema, host, port);
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
            StringBuffer buffer = new StringBuffer();
            String[] queryList = queryString.split("&");
            for (String query : queryList) {
                int index = query.indexOf("=");
                String key = index == -1 ? query : query.substring(0, index);
                if (queryKeys.contains(key)) continue;
                if (buffer.length() > 0) buffer.append("&");
                buffer.append(query);
            }
            String protocol = requestURL.getProtocol();
            int port = requestURL.getPort();
            String path = port < 0 ?
                    String.format("%s://%s%s", protocol, requestURL.getHost(), requestURL.getPath()) :
                    String.format("%s://%s:%d%s", protocol, requestURL.getHost(), port, requestURL.getPath());
            return buffer.length() > 0 ? String.format("%s?%s", path, buffer) : path;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return url;
        }
    }
}
