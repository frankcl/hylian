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

    private static final Map<String, HttpSession> sessionMap = new ConcurrentHashMap<>();

    /**
     * 添加session
     *
     * @param httpSession HTTP会话
     */
    public static void put(HttpSession httpSession) {
        if (httpSession == null) return;
        sessionMap.put(httpSession.getId(), httpSession);
        logger.info("put session[{}] success", httpSession.getId());
    }

    /**
     * 移除session
     *
     * @param httpSession HTTP会话
     */
    public static void remove(HttpSession httpSession) {
        if (httpSession == null || !sessionMap.containsKey(httpSession.getId())) return;
        sessionMap.remove(httpSession.getId());
        logger.info("remove session[{}] success", httpSession.getId());
    }

    /**
     * 失效session
     *
     * @param sessionId 会话ID
     */
    public static void invalidate(String sessionId) {
        HttpSession httpSession = sessionMap.get(sessionId);
        if (httpSession == null) {
            logger.warn("session[{}] is not found", sessionId);
            return;
        }
        httpSession.invalidate();
        logger.info("session[{}] is invalidated", httpSession.getId());
    }
}
