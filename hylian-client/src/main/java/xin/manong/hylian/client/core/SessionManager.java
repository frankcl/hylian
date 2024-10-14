package xin.manong.hylian.client.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Set;
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
    private static final Set<String> tokenSessions = ConcurrentHashMap.newKeySet();

    /**
     * 添加session
     *
     * @param httpSession HTTP会话
     */
    public static void put(HttpSession httpSession) {
        if (httpSession == null) return;
        sessionMap.put(httpSession.getId(), httpSession);
        logger.debug("put session[{}] success", httpSession.getId());
    }

    /**
     * 添加token会话
     *
     * @param httpSession HTTP会话
     */
    public static void putTokenSession(HttpSession httpSession) {
        if (httpSession == null) return;
        tokenSessions.add(httpSession.getId());
        logger.debug("put token session[{}] success", httpSession.getId());
    }

    /**
     * 判断是否为token会话
     *
     * @param sessionId 会话ID
     * @return token会话返回true，否则返回false
     */
    public static boolean isTokenSession(String sessionId) {
        return tokenSessions.contains(sessionId);
    }

    /**
     * 移除session
     *
     * @param httpSession HTTP会话
     */
    public static void remove(HttpSession httpSession) {
        if (httpSession == null) return;
        String sessionId = httpSession.getId();
        sessionMap.remove(sessionId);
        tokenSessions.remove(sessionId);
        logger.debug("remove session[{}] success", httpSession.getId());
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
        logger.debug("session[{}] is invalidated", httpSession.getId());
    }
}
