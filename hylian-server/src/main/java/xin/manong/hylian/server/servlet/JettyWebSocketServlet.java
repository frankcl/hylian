package xin.manong.hylian.server.servlet;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import xin.manong.hylian.server.websocket.QRCodeWebSocket;

import javax.servlet.annotation.WebServlet;

/**
 * WebSocket servlet定义
 * 1. 设置WebSocket最大超时时间5分钟
 * 2. 注册微信小程序码WebSocket处理服务
 *
 * @author frankcl
 * @date 2024-11-08 18:44:36
 */
@WebServlet(urlPatterns = { "/api/ws/qrcode" })
public class JettyWebSocketServlet extends WebSocketServlet {

    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.getPolicy().setIdleTimeout(300000);
        webSocketServletFactory.register(QRCodeWebSocket.class);
    }
}
