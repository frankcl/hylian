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
import xin.manong.security.keeper.server.dao.mapper.AppLoginMapper;
import xin.manong.security.keeper.model.AppLogin;
import xin.manong.security.keeper.server.service.AppLoginService;
import xin.manong.security.keeper.server.service.request.AppLoginSearchRequest;

import javax.annotation.Resource;
import java.util.List;

/**
 * 应用登录信息服务实现
 *
 * @author frankcl
 * @date 2023-09-04 10:53:42
 */
@Service
public class AppLoginServiceImpl implements AppLoginService {

    private static final Logger logger = LoggerFactory.getLogger(AppLoginServiceImpl.class);

    @Resource
    protected AppLoginMapper appLoginMapper;

    @Override
    public boolean add(AppLogin appLogin) {
        LambdaQueryWrapper<AppLogin> query = new LambdaQueryWrapper<>();
        query.eq(AppLogin::getSessionId, appLogin.sessionId).eq(AppLogin::getAppId, appLogin.appId);
        if (appLoginMapper.selectCount(query) > 0) {
            logger.error("user has login for app[{}] and session[{}]", appLogin.appId, appLogin.sessionId);
            throw new RuntimeException(String.format("用户已登录应用[%s]", appLogin.appId));
        }
        return appLoginMapper.insert(appLogin) > 0;
    }

    @Override
    public int removeExpiredAppLogins(Long maxUpdateTime) {
        LambdaQueryWrapper<AppLogin> query = new LambdaQueryWrapper<>();
        query.lt(AppLogin::getUpdateTime, maxUpdateTime);
        return appLoginMapper.delete(query);
    }

    @Override
    public boolean removeAppLogins(String ticketId) {
        LambdaQueryWrapper<AppLogin> query = new LambdaQueryWrapper<>();
        query.eq(AppLogin::getTicketId, ticketId);
        return appLoginMapper.delete(query) > 0;
    }

    @Override
    public boolean removeAppLogin(String sessionId, String appId) {
        LambdaQueryWrapper<AppLogin> query = new LambdaQueryWrapper<>();
        query.eq(AppLogin::getSessionId, sessionId).eq(AppLogin::getAppId, appId);
        return appLoginMapper.delete(query) > 0;
    }

    @Override
    public boolean isLoginApp(AppLoginSearchRequest searchRequest) {
        if (StringUtils.isEmpty(searchRequest.userId)) {
            logger.error("user id is empty");
            throw new RuntimeException("用户ID为空");
        }
        if (StringUtils.isEmpty(searchRequest.appId)) {
            logger.error("app id is empty");
            throw new RuntimeException("应用ID为空");
        }
        if (StringUtils.isEmpty(searchRequest.sessionId)) {
            logger.error("session id is empty");
            throw new RuntimeException("会话ID为空");
        }
        LambdaQueryWrapper<AppLogin> query = new LambdaQueryWrapper<>();
        query.eq(AppLogin::getUserId, searchRequest.userId).eq(AppLogin::getAppId, searchRequest.appId).
                eq(AppLogin::getSessionId, searchRequest.sessionId);
        return appLoginMapper.selectCount(query) > 0;
    }

    @Override
    public List<AppLogin> getAppLogins(String ticketId) {
        LambdaQueryWrapper<AppLogin> query = new LambdaQueryWrapper<>();
        query.eq(AppLogin::getTicketId, ticketId);
        return appLoginMapper.selectList(query);
    }

    @Override
    public Pager<AppLogin> search(AppLoginSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new AppLoginSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        LambdaQueryWrapper<AppLogin> query = new LambdaQueryWrapper<>();
        query.orderByDesc(AppLogin::getCreateTime);
        if (!StringUtils.isEmpty(searchRequest.appId)) query.eq(AppLogin::getAppId, searchRequest.appId);
        if (!StringUtils.isEmpty(searchRequest.userId)) query.eq(AppLogin::getUserId, searchRequest.userId);
        if (!StringUtils.isEmpty(searchRequest.sessionId)) query.eq(AppLogin::getSessionId, searchRequest.sessionId);
        IPage<AppLogin> page = appLoginMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
