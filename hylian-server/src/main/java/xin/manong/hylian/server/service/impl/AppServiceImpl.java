package xin.manong.hylian.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.dao.mapper.AppMapper;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.model.App;
import xin.manong.hylian.server.service.AppService;
import xin.manong.hylian.server.service.AppUserService;
import xin.manong.hylian.server.service.PermissionService;
import xin.manong.hylian.server.service.RoleService;
import xin.manong.hylian.server.service.request.AppSearchRequest;
import xin.manong.hylian.server.util.ModelValidator;

import java.util.List;

/**
 * 应用服务实现
 *
 * @author frankcl
 * @date 2023-09-01 13:54:30
 */
@Service
public class AppServiceImpl implements AppService {

    @Resource
    protected AppMapper appMapper;
    @Lazy
    @Resource
    protected RoleService roleService;
    @Lazy
    @Resource
    protected PermissionService permissionService;
    @Lazy
    @Resource
    protected AppUserService appUserService;

    @Override
    public App get(String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("应用ID为空");
        return appMapper.selectById(id);
    }

    @Override
    public boolean add(App app) {
        LambdaQueryWrapper<App> query = new LambdaQueryWrapper<>();
        query.eq(App::getId, app.id).or().eq(App::getName, app.name);
        if (appMapper.selectCount(query) > 0) throw new IllegalStateException("应用已存在");
        return appMapper.insert(app) > 0;
    }

    @Override
    public boolean update(App app) {
        App prevApp = appMapper.selectById(app.id);
        if (prevApp == null) throw new NotFoundException("应用不存在");
        if (StringUtils.isEmpty(app.name)) app.name = null;
        if (app.name != null && !app.name.equals(prevApp.name)) {
            LambdaQueryWrapper<App> query = new LambdaQueryWrapper<>();
            query.eq(App::getName, app.name);
            if (appMapper.selectCount(query) > 0) throw new IllegalStateException("应用已存在");
        }
        return appMapper.updateById(app) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("应用ID为空");
        boolean success = appMapper.deleteById(id) > 0;
        if (success) {
            permissionService.deleteByApp(id);
            roleService.deleteByApp(id);
            appUserService.deleteByApp(id);
        }
        return success;
    }

    @Override
    public List<App> getApps() {
        return appMapper.selectList(null);
    }

    @Override
    public Pager<App> search(AppSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new AppSearchRequest();
        if (searchRequest.pageNum == null || searchRequest.pageNum < 1) searchRequest.pageNum = Constants.DEFAULT_PAGE_NUM;
        if (searchRequest.pageSize == null || searchRequest.pageSize <= 0) searchRequest.pageSize = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(App.class, searchRequest);
        QueryWrapper<App> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (!StringUtils.isEmpty(searchRequest.name)) query.like("name", searchRequest.name);
        if (!StringUtils.isEmpty(searchRequest.id)) query.eq("id", searchRequest.id);
        if (searchRequest.appIds != null) query.in("id", searchRequest.appIds);
        IPage<App> page = appMapper.selectPage(new Page<>(searchRequest.pageNum, searchRequest.pageSize), query);
        return Converter.convert(page);
    }

    @Override
    public void verifyApp(String appId, String appSecret) {
        if (StringUtils.isEmpty(appId)) throw new NotAuthorizedException("应用ID为空");
        if (StringUtils.isEmpty(appSecret)) throw new NotAuthorizedException("应用秘钥为空");
        App app = get(appId);
        if (app == null) throw new NotAuthorizedException("应用不存在");
        if (!app.secret.equals(appSecret)) throw new NotAuthorizedException("应用秘钥不匹配");
    }
}
