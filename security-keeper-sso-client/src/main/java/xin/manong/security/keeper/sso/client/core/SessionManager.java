package xin.manong.security.keeper.sso.client.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * session管理器
 *
 * @author frankcl
 * @date 2023-09-11 16:51:33
 */
public class SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private static Map<String, HttpSession> sessionMap = new ConcurrentHashMap<>();

    /**
     * 放入session进行管理
     *
     * @param httpSession HTTP session
     */
    public static void putSession(HttpSession httpSession) {
        if (httpSession == null) {
            logger.warn("session is null");
            return;
        }
        sessionMap.put(httpSession.getId(), httpSession);
        logger.info("add session[{}]", httpSession.getId());
    }

    /**
     * 失效session
     *
     * @param sessionId session ID
     */
    public static void invalidateSession(String sessionId) {
        HttpSession httpSession = sessionMap.remove(sessionId);
        if (httpSession == null) {
            logger.warn("session[{}] is not found", sessionId);
            return;
        }
        logger.info("session[{}] is invalidated", httpSession.getId());
        httpSession.invalidate();
    }
}
