package xin.manong.hylian.server.monitor;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.hylian.server.service.ActivityService;
import xin.manong.hylian.server.service.QRCodeService;
import xin.manong.hylian.server.websocket.QRCodeWebSocket;

/**
 * 活动记录清理
 * 定期清除过期活动记录
 *
 * @author frankcl
 * @date 2023-09-14 16:44:27
 */
public class Sweeper implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Sweeper.class);

    private static final Long CHECK_INTERVAL_MS = 300000L;
    private static final Long EXPIRED_INTERVAL_MS = 86400000L;

    private volatile boolean running = false;
    private Thread runThread;
    @Resource
    protected ActivityService activityService;
    @Resource
    protected QRCodeService qrCodeService;

    /**
     * 启动清理
     */
    public void start() {
        logger.info("sweeper is starting ...");
        running = true;
        runThread = new Thread(this, "sweeper");
        runThread.start();
        logger.info("sweeper has been started");
    }

    /**
     * 停止清理
     */
    public void stop() {
        logger.info("sweeper is stopping ...");
        running = false;
        if (runThread.isAlive()) runThread.interrupt();
        try {
            runThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("sweeper has been stopped");
    }

    /**
     * 清理逻辑
     */
    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        while (running) {
            try {
                Long maxUpdateTime = System.currentTimeMillis() - EXPIRED_INTERVAL_MS;
                int n = activityService.removeExpires(maxUpdateTime);
                logger.info("sweep {} expired activities", n);
                Long before = System.currentTimeMillis() - 60000L;
                QRCodeWebSocket.removeExpires(before);
                n = qrCodeService.deleteExpires(before);
                logger.info("sweep {} expired QRCodes", n);
                Thread.sleep(CHECK_INTERVAL_MS);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
