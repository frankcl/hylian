package xin.manong.hylian.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.dao.mapper.AppMapper;
import xin.manong.hylian.model.Pager;
import xin.manong.hylian.model.App;
import xin.manong.hylian.server.service.AppService;
import xin.manong.hylian.server.service.request.AppSearchRequest;
import xin.manong.hylian.server.util.ModelValidator;

import javax.annotation.Resource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;

/**
 * 应用服务实现
 *
 * @author frankcl
 * @date 2023-09-01 13:54:30
 */
@Service
public class AppServiceImpl implements AppService {

    private static final Logger logger = LoggerFactory.getLogger(AppServiceImpl.class);

    @Resource
    protected AppMapper appMapper;

    @Override
    public App get(String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("app id is empty when getting");
            throw new BadRequestException("应用ID为空");
        }
        return appMapper.selectById(id);
    }

    @Override
    public boolean add(App app) {
        LambdaQueryWrapper<App> query = new LambdaQueryWrapper<>();
        query.eq(App::getId, app.id).or().eq(App::getName, app.name);
        if (appMapper.selectCount(query) > 0) {
            logger.error("app has existed for the same id[{}] or name[{}]", app.id, app.name);
            throw new IllegalStateException("同名应用已存在");
        }
        return appMapper.insert(app) > 0;
    }

    @Override
    public boolean update(App app) {
        App prevApp = appMapper.selectById(app.id);
        if (prevApp == null) {
            logger.error("app is not found for id[{}]", app.id);
            throw new NotFoundException("应用不存在");
        }
        if (StringUtils.isEmpty(app.name)) app.name = null;
        if (app.name != null && !app.name.equals(prevApp.name)) {
            LambdaQueryWrapper<App> query = new LambdaQueryWrapper<>();
            query.eq(App::getName, app.name);
            if (appMapper.selectCount(query) > 0) {
                logger.error("app has existed for the same name[{}]", app.name);
                throw new IllegalStateException("同名应用已存在");
            }
        }
        return appMapper.updateById(app) > 0;
    }

    @Override
    public boolean delete(String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("app id is empty when deleting");
            throw new BadRequestException("应用ID为空");
        }
        return appMapper.deleteById(id) > 0;
    }

    @Override
    public Pager<App> search(AppSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new AppSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(App.class, searchRequest);
        QueryWrapper<App> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (!StringUtils.isEmpty(searchRequest.name)) query.like("name", searchRequest.name);
        IPage<App> page = appMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }

    @Override
    public void verifyApp(String appId, String appSecret) {
        if (StringUtils.isEmpty(appId)) {
            logger.error("app id is empty");
            throw new NotAuthorizedException("应用ID为空");
        }
        if (StringUtils.isEmpty(appSecret)) {
            logger.error("app secret is empty");
            throw new NotAuthorizedException("应用秘钥为空");
        }
        App app = get(appId);
        if (app == null) {
            logger.error("app[{}] is not found", appId);
            throw new NotAuthorizedException("应用不存在");
        }
        if (!app.secret.equals(appSecret)) {
            logger.error("app secret not matched");
            throw new NotAuthorizedException("应用秘钥不匹配");
        }
    }
}
