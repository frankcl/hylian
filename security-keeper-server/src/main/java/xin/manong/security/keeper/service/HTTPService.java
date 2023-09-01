package xin.manong.security.keeper.service;

import javax.servlet.http.HttpServletRequest;

/**
 * HTTP服务接口定义
 *
 * @author frankcl
 * @date 2023-09-01 17:51:55
 */
public interface HTTPService {

    /**
     * 获取HTTP请求URL
     *
     * @param httpRequest HTTP请求
     * @return HTTP请求URL
     */
    String getRequestURL(HttpServletRequest httpRequest);
}
