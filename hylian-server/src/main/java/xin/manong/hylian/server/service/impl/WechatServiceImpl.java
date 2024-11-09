package xin.manong.hylian.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import xin.manong.hylian.server.dao.mapper.WechatMapper;
import xin.manong.hylian.server.model.Wechat;
import xin.manong.hylian.server.service.WechatService;

import javax.annotation.Resource;

/**
 * 微信服务实现
 *
 * @author frankcl
 * @date 2024-11-04 11:43:12
 */
@Service
public class WechatServiceImpl implements WechatService {

    private static final String CATEGORY_MINI = "mini";

    @Resource
    protected WechatMapper wechatMapper;

    @Override
    public Wechat getMiniWeChat() {
        LambdaQueryWrapper<Wechat> query = new LambdaQueryWrapper<>();
        query.eq(Wechat::getCategory, CATEGORY_MINI);
        return wechatMapper.selectOne(query);
    }
}
