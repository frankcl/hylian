package xin.manong.security.keeper.server.service;

/**
 * 验证码服务接口定义
 *
 * @author frankcl
 * @date 2024-09-21 17:12:33
 */
public interface CaptchaService {

    /**
     * 创建验证码
     * @param sessionId 会话ID
     *
     * @return 验证码
     */
    String create(String sessionId);

    /**
     * 获取验证码
     *
     * @param sessionId 会话ID
     * @return 成功返回验证码，否则返回null
     */
    String get(String sessionId);

    /**
     * 移除验证码
     *
     * @param sessionId 会话ID
     */
    void remove(String sessionId);
}
