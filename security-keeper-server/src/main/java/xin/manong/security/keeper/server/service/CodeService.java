package xin.manong.security.keeper.server.service;

/**
 * 安全码服务接口定义
 *
 * @author frankcl
 * @date 2023-09-01 16:27:05
 */
public interface CodeService {

    /**
     * 根据ticket创建安全码
     *
     * @param ticket
     * @return 安全码
     */
    String createCode(String ticket);

    /**
     * 根据安全码获取ticket
     *
     * @param code 安全码
     * @return 存在返回ticket，否则返回null
     */
    String getTicket(String code);

    /**
     * 移除安全码
     *
     * @param code 安全码
     * @return 成功返回true，否则返回false
     */
    boolean removeCode(String code);
}
