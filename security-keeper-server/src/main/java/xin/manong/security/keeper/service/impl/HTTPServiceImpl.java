package xin.manong.security.keeper.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xin.manong.security.keeper.service.HTTPService;

import javax.servlet.http.HttpServletRequest;

/**
 * HTTP服务实现
 *
 * @author frankcl
 * @date 2023-09-01 17:52:54
 */
@Service
public class HTTPServiceImpl implements HTTPService {

    @Override
    public String getRequestURL(HttpServletRequest httpRequest) {
        String requestURL = httpRequest.getRequestURL().toString();
        String query = httpRequest.getQueryString();
        return StringUtils.isEmpty(query) ? requestURL : String.format("%s?%s", requestURL, query);
    }
}
