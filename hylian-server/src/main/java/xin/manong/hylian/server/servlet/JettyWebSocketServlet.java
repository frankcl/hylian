package xin.manong.hylian.server.servlet;

import jakarta.servlet.annotation.WebServlet;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServletFactory;
import xin.manong.hylian.server.websocket.QRCodeWebSocket;

import java.time.Duration;


/**
 * WebSocket servlet定义
 * 1. 设置WebSocket最大超时时间5分钟
 * 2. 注册微信小程序码WebSocket处理服务
 *
 * @author frankcl
 * @date 2024-11-08 18:44:36
 */
@WebServlet(urlPatterns = { "/api/ws/qrcode" })
public class JettyWebSocketServlet extends org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServlet {

    @Override
    public void configure(JettyWebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.setIdleTimeout(Duration.ofMillis(300000L));
        webSocketServletFactory.register(QRCodeWebSocket.class);
    }
}
