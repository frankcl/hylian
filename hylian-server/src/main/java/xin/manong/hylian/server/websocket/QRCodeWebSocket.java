package xin.manong.hylian.server.websocket;

import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author frankcl
 * @date 2024-11-08 18:22:36
 */
@WebSocket
public class QRCodeWebSocket {

    private static final Logger logger = LoggerFactory.getLogger(QRCodeWebSocket.class);

    private static final Map<String, QRCodeWebSocket> webSocketMap = new ConcurrentHashMap<>();

    private String key;
    private Session session;
    private Long createTime;

    @OnWebSocketOpen
    public void onOpen(Session session) {
        String key = getQueryKey(session);
        if (StringUtils.isEmpty(key)) {
            logger.error("param key is missing");
            session.close();
            throw new IllegalArgumentException("参数key为空");
        }
        QRCodeWebSocket prevWebSocket = webSocketMap.get(key);
        if (prevWebSocket != null) {
            prevWebSocket.session.close();
            webSocketMap.remove(key);
            logger.warn("session existed for key[{}], close it", key);
        }
        this.key = key;
        this.session = session;
        this.createTime = System.currentTimeMillis();
        webSocketMap.put(key, this);
        logger.info("open session success for key[{}]", key);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        if (key != null) webSocketMap.remove(key);
        logger.info("close session for key[{}], code[{}] and reason[{}]", key, statusCode, reason);
    }

    @OnWebSocketError
    public void onError(Throwable throwable) {
        logger.error("websocket error: {}", throwable.getMessage());
        logger.error(throwable.getMessage(), throwable);
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        logger.info("receive message[{}]", message);
    }

    /**
     * 发送消息
     *
     * @param key 微信小程序码key
     * @param message 消息
     * @throws IOException I/O异常
     */
    public static void sendMessage(String key, String message) throws IOException {
        QRCodeWebSocket webSocket = webSocketMap.get(key);
        if (webSocket == null) {
            logger.error("websocket is not found for key[{}]", key);
            throw new NotFoundException("WebSocket尚未建立");
        }
        webSocket.session.sendText(message, null);
    }

    /**
     * 清除过期endpoint
     *
     * @param before 最小时间戳，单位：毫秒
     */
    public static void removeExpires(Long before) throws IOException {
        if (before == null) before = System.currentTimeMillis() - 60000L;
        Iterator<Map.Entry<String, QRCodeWebSocket>> iterator = webSocketMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, QRCodeWebSocket> entry = iterator.next();
            QRCodeWebSocket webSocket = entry.getValue();
            if (webSocket.createTime < before) {
                logger.info("websocket is expired for key[{}]", entry.getKey());
                webSocket.session.close();
                iterator.remove();
            }
        }
    }

    /**
     * 获取URI参数key对应值
     *
     * @param session 会话
     * @return 成功返回参数key对应值，否则返回null
     */
    private String getQueryKey(Session session) {
        URI uri = session.getUpgradeRequest().getRequestURI();
        String queryString = uri.getQuery();
        if (StringUtils.isEmpty(queryString)) return null;
        String[] queries = queryString.split("&");
        for (String query : queries) {
            String[] kv = query.split("=");
            if (kv.length != 2) continue;
            if (kv[0].equals("key")) return kv[1];
        }
        return null;
    }
}
