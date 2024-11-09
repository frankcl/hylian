package xin.manong.hylian.server.service;

import xin.manong.hylian.server.model.QRCode;

/**
 * 小程序码服务接口
 *
 * @author frankcl
 * @date 2024-11-04 22:34:42
 */
public interface QRCodeService {

    /**
     * 根据key获取小程序码
     *
     * @param key 小程序码key
     * @return 成功返回小程序码，否则返回null
     */
    QRCode getByKey(String key);

    /**
     * 添加小程序码
     *
     * @param qrCode 小程序码
     * @return 成功返回true，否则返回false
     */
    boolean add(QRCode qrCode);

    /**
     * 更新小程序码
     *
     * @param qrCode 小程序码
     * @return 成功返回true，否则返回false
     */
    boolean update(QRCode qrCode);

    /**
     * 更新小程序码
     *
     * @param qrCode 小程序码
     * @return 成功返回true，否则返回false
     */
    boolean updateByKey(QRCode qrCode);

    /**
     * 根据key删除小程序码
     *
     * @param key 小程序码key
     * @return 成功返回true，否则返回false
     */
    boolean deleteByKey(String key);

    /**
     * 删除过期小程序码：删除创建时间小于before的小程序码
     *
     * @param before 过期时间戳，单位：毫秒
     * @return 删除过期记录数量
     */
    int deleteExpires(Long before);
}
