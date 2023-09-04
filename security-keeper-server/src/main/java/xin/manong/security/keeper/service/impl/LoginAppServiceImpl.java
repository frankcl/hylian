package xin.manong.security.keeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.security.keeper.dao.mapper.LoginAppMapper;
import xin.manong.security.keeper.model.LoginApp;
import xin.manong.security.keeper.service.LoginAppService;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户应用登录服务实现
 *
 * @author frankcl
 * @date 2023-09-04 10:53:42
 */
@Service
public class LoginAppServiceImpl implements LoginAppService {

    private static final Logger logger = LoggerFactory.getLogger(LoginAppServiceImpl.class);

    @Resource
    protected LoginAppMapper loginAppMapper;

    @Override
    public boolean register(LoginApp loginApp) {
        LambdaQueryWrapper<LoginApp> query = new LambdaQueryWrapper<>();
        query.eq(LoginApp::getTicketId, loginApp.ticketId).eq(LoginApp::getAppId, loginApp.appId);
        if (loginAppMapper.selectCount(query) > 0) {
            logger.error("login app has existed for the same ticket id[{}] and app id[{}]",
                    loginApp.ticketId, loginApp.appId);
            throw new RuntimeException(String.format("相同ticket[%s]和应用[%s]已登录",
                    loginApp.ticketId, loginApp.appId));
        }
        return loginAppMapper.insert(loginApp) > 0;
    }

    @Override
    public void removeLoginApps(String ticketId) {
        LambdaQueryWrapper<LoginApp> query = new LambdaQueryWrapper<>();
        query.eq(LoginApp::getTicketId, ticketId);
        int n = loginAppMapper.delete(query);
        if (n > 0) logger.info("delete login app records[{}]", n);
    }

    @Override
    public boolean isLoginApp(String ticketId, String appId) {
        LambdaQueryWrapper<LoginApp> query = new LambdaQueryWrapper<>();
        query.eq(LoginApp::getTicketId, ticketId).eq(LoginApp::getAppId, appId);
        return loginAppMapper.selectCount(query) > 0;
    }

    @Override
    public List<LoginApp> getLoginApps(String ticketId) {
        LambdaQueryWrapper<LoginApp> query = new LambdaQueryWrapper<>();
        query.eq(LoginApp::getTicketId, ticketId);
        return loginAppMapper.selectList(query);
    }
}
