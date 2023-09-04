package xin.manong.security.keeper.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.security.keeper.service.HTTPService;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;

/**
 * HTTP服务实现
 *
 * @author frankcl
 * @date 2023-09-01 17:52:54
 */
@Service
public class HTTPServiceImpl implements HTTPService {

    private static final Logger logger = LoggerFactory.getLogger(HTTPServiceImpl.class);

    @Override
    public String getRequestURL(HttpServletRequest httpRequest) {
        String requestURL = httpRequest.getRequestURL().toString();
        String query = httpRequest.getQueryString();
        return StringUtils.isEmpty(query) ? requestURL : String.format("%s?%s", requestURL, query);
    }

    @Override
    public String getRequestBaseURL(HttpServletRequest httpRequest) {
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
}
