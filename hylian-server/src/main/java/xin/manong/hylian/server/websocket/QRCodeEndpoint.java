package xin.manong.hylian.server.websocket;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信小程序码websocket endpoint
 *
 * @author frankcl
 * @date 2024-11-07 21:28:11
 */
@ServerEndpoint("/api/ws/qrcode/{key}")
public class QRCodeEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(QRCodeEndpoint.class);

    private static final Map<String, QRCodeEndpoint> endpointMap = new ConcurrentHashMap<>();

    private String key;
    private Session session;
    private Long createTime;

    @OnOpen
    public void onOpen(Session session, @PathParam("key") String key) throws IOException {
        if (StringUtils.isEmpty(key)) {
            logger.error("QRCode key is empty");
            session.close();
            throw new IllegalArgumentException("小程序码key为空");
        }
        QRCodeEndpoint prevEndpoint = endpointMap.get(key);
        if (prevEndpoint != null) {
            try {
                prevEndpoint.session.close();
                logger.warn("session existed for key[{}], close it", key);
            } catch (IOException e) {
                logger.error("close session failed for key[{}]", key);
                logger.error(e.getMessage(), e);
            } finally {
                endpointMap.remove(key);
            }
        }
        this.key = key;
        this.session = session;
        this.createTime = System.currentTimeMillis();
        endpointMap.put(key, this);
        logger.info("open session success for key[{}]", key);
    }

    @OnClose
    public void onClose() {
        if (key != null) endpointMap.remove(key);
        logger.info("close session success for key[{}]", key);
    }

    @OnError
    public void onError(Throwable throwable) {
        logger.error("QRCode websocket error: {}", throwable.getMessage());
        logger.error(throwable.getMessage(), throwable);
    }

    @OnMessage
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
        QRCodeEndpoint endpoint = endpointMap.get(key);
        if (endpoint == null) {
            logger.error("endpoint is not found for key[{}]", key);
            throw new NotFoundException("小程序码WebSocket不存在");
        }
        endpoint.session.getBasicRemote().sendText(message);
    }

    /**
     * 清除过期endpoint
     *
     * @param before 最小时间戳，单位：毫秒
     */
    public static void removeExpires(Long before) throws IOException {
        if (before == null) before = System.currentTimeMillis() - 60000L;
        Iterator<Map.Entry<String, QRCodeEndpoint>> iterator = endpointMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, QRCodeEndpoint> entry = iterator.next();
            QRCodeEndpoint endpoint = entry.getValue();
            if (endpoint.createTime < before) {
                logger.info("QRCode endpoint is expired for key[{}]", entry.getKey());
                endpoint.session.close();
                iterator.remove();
            }
        }
    }
}
