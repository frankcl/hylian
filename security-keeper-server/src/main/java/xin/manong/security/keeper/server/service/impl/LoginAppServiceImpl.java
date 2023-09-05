package xin.manong.security.keeper.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.server.common.Constants;
import xin.manong.security.keeper.server.converter.Converter;
import xin.manong.security.keeper.server.dao.mapper.LoginAppMapper;
import xin.manong.security.keeper.model.LoginApp;
import xin.manong.security.keeper.server.service.LoginAppService;
import xin.manong.security.keeper.server.service.request.LoginAppSearchRequest;

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

    @Override
    public Pager<LoginApp> search(LoginAppSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new LoginAppSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        LambdaQueryWrapper<LoginApp> query = new LambdaQueryWrapper<>();
        query.orderByDesc(LoginApp::getCreateTime);
        if (!StringUtils.isEmpty(searchRequest.appId)) query.eq(LoginApp::getAppId, searchRequest.appId);
        if (!StringUtils.isEmpty(searchRequest.userId)) query.eq(LoginApp::getUserId, searchRequest.userId);
        IPage<LoginApp> page = loginAppMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
