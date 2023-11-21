package xin.manong.security.keeper.server.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.security.keeper.server.service.AppLoginService;

import javax.annotation.Resource;

/**
 * 应用登录信息清除监控
 *
 * @author frankcl
 * @date 2023-09-14 16:44:27
 */
public class SweepMonitor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SweepMonitor.class);

    private static final Long DEFAULT_CHECK_INTERVAL_MS = 300000L;
    private static final Long DEFAULT_EXPIRED_INTERVAL_MS = 86400000L;

    private volatile boolean running = false;
    private Long checkIntervalMs = DEFAULT_CHECK_INTERVAL_MS;
    private Long expiredIntervalMs = DEFAULT_EXPIRED_INTERVAL_MS;
    private String name = "sweep-monitor";
    private Thread monitorThread;
    @Resource
    protected AppLoginService appLoginService;

    /**
     * 启动监控
     */
    public void start() {
        logger.info("sweep monitor is starting ...");
        running = true;
        monitorThread = new Thread(this, name);
        monitorThread.start();
        logger.info("sweep monitor has been started");
    }

    /**
     * 停止监控
     */
    public void stop() {
        logger.info("sweep monitor is stopping ...");
        running = false;
        if (monitorThread.isAlive()) monitorThread.interrupt();
        try {
            monitorThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("sweep monitor has been stopped");
    }

    /**
     * 监控逻辑
     */
    @Override
    public void run() {
        while (running) {
            try {
                Long maxUpdateTime = System.currentTimeMillis() - expiredIntervalMs;
                int n = appLoginService.removeExpiredAppLogins(maxUpdateTime);
                logger.info("sweep expired app login records[{}]", n);
                Thread.sleep(checkIntervalMs);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
