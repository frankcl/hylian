package xin.manong.hylian.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xin.manong.hylian.server.dao.mapper.QRCodeMapper;
import xin.manong.hylian.server.model.QRCode;
import xin.manong.hylian.server.service.QRCodeService;

import javax.annotation.Resource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

/**
 * 小程序码服务实现
 *
 * @author frankcl
 * @date 2024-11-04 22:38:36
 */
@Service
public class QRCodeServiceImpl implements QRCodeService {

    @Resource
    protected QRCodeMapper qrCodeMapper;

    @Override
    public QRCode getByKey(String key) {
        if (StringUtils.isEmpty(key)) throw new BadRequestException("小程序码key为空");
        LambdaQueryWrapper<QRCode> query = new LambdaQueryWrapper<>();
        query.eq(QRCode::getKey, key);
        return qrCodeMapper.selectOne(query);
    }

    @Override
    public boolean add(QRCode qrCode) {
        if (qrCode == null) throw new BadRequestException("添加小程序码为空");
        return qrCodeMapper.insert(qrCode) > 0;
    }

    @Override
    public boolean update(QRCode qrCode) {
        if (qrCode == null) throw new BadRequestException("更新小程序码为空");
        if (qrCodeMapper.selectById(qrCode.id) == null) throw new NotFoundException("小程序码不存在");
        return qrCodeMapper.updateById(qrCode) > 0;
    }

    @Override
    public boolean updateByKey(QRCode qrCode) {
        if (StringUtils.isEmpty(qrCode.key)) throw new BadRequestException("小程序码key为空");
        if (qrCode.status != null && (qrCode.status < QRCode.STATUS_ERROR ||
                qrCode.status > QRCode.STATUS_AUTHORIZED)) throw new BadRequestException("小程序码状态非法");
        LambdaQueryWrapper<QRCode> query = new LambdaQueryWrapper<>();
        query.eq(QRCode::getKey, qrCode.key);
        return qrCodeMapper.update(qrCode, query) > 0;
    }

    @Override
    public boolean deleteByKey(String key) {
        LambdaQueryWrapper<QRCode> query = new LambdaQueryWrapper<>();
        query.eq(QRCode::getKey, key);
        return qrCodeMapper.delete(query) > 0;
    }

    @Override
    public int deleteExpires(Long before) {
        if (before == null) before = System.currentTimeMillis() - 60000L;
        LambdaQueryWrapper<QRCode> query = new LambdaQueryWrapper<>();
        query.lt(QRCode::getCreateTime, before);
        return qrCodeMapper.delete(query);
    }
}
