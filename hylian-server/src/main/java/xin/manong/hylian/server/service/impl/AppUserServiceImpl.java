package xin.manong.hylian.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.hylian.model.AppUser;
import xin.manong.hylian.model.User;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.dao.mapper.AppUserMapper;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.server.service.AppUserService;
import xin.manong.hylian.server.service.UserService;
import xin.manong.hylian.server.service.request.AppUserSearchRequest;
import xin.manong.hylian.server.util.ModelValidator;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用用户关系服务实现
 *
 * @author frankcl
 * @date 2024-10-13 18:41:36
 */
@Service
public class AppUserServiceImpl implements AppUserService {

    private static final Logger logger = LoggerFactory.getLogger(AppUserServiceImpl.class);

    @Resource
    protected AppUserMapper appUserMapper;
    @Lazy
    @Resource
    protected UserService userService;

    @Override
    public boolean add(AppUser appUser) {
        LambdaQueryWrapper<AppUser> query = new LambdaQueryWrapper<>();
        query.eq(AppUser::getUserId, appUser.getUserId()).eq(AppUser::getAppId, appUser.getAppId());
        if (appUserMapper.selectCount(query) > 0) throw new IllegalStateException("应用用户关系已存在");
        return appUserMapper.insert(appUser) > 0;
    }

    @Override
    public AppUser getAppUser(String appId, String userId) {
        if (StringUtils.isEmpty(appId)) throw new BadRequestException("应用ID为空");
        if (StringUtils.isEmpty(userId)) throw new BadRequestException("用户ID为空");
        LambdaQueryWrapper<AppUser> query = new LambdaQueryWrapper<>();
        query.eq(AppUser::getAppId, appId).eq(AppUser::getUserId, userId);
        return appUserMapper.selectOne(query);
    }

    @Override
    public List<AppUser> getByAppId(String appId) {
        if (StringUtils.isEmpty(appId)) throw new BadRequestException("应用ID为空");
        LambdaQueryWrapper<AppUser> query = new LambdaQueryWrapper<>();
        query.eq(AppUser::getAppId, appId);
        return appUserMapper.selectList(query);
    }

    @Override
    public List<AppUser> getByUserId(String userId) {
        if (StringUtils.isEmpty(userId)) throw new BadRequestException("用户ID为空");
        LambdaQueryWrapper<AppUser> query = new LambdaQueryWrapper<>();
        query.eq(AppUser::getUserId, userId);
        return appUserMapper.selectList(query);
    }

    @Override
    public List<User> getUsersByApp(String appId) {
        List<AppUser> appUsers = getByAppId(appId);
        List<String> userIds = appUsers.stream().map(r -> r.userId).collect(Collectors.toList());
        return userService.batchGet(userIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(List<AppUser> addAppUsers, List<Long> removeAppUsers) {
        for (AppUser appUser : addAppUsers) {
            if (!add(appUser)) throw new InternalServerErrorException("添加应用用户关系失败");
        }
        for (Long id : removeAppUsers) {
            if (!delete(id)) throw new InternalServerErrorException("删除应用用户关系失败");
        }
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) throw new BadRequestException("应用用户关系ID为空");
        return appUserMapper.deleteById(id) > 0;
    }

    @Override
    public void deleteByApp(String appId) {
        if (StringUtils.isEmpty(appId)) throw new BadRequestException("应用ID为空");
        LambdaQueryWrapper<AppUser> query = new LambdaQueryWrapper<>();
        query.eq(AppUser::getAppId, appId);
        int n = appUserMapper.delete(query);
        logger.info("delete app user num[{}] for app[{}]", n, appId);
    }

    @Override
    public void deleteByUser(String userId) {
        if (StringUtils.isEmpty(userId)) throw new BadRequestException("用户ID为空");
        LambdaQueryWrapper<AppUser> query = new LambdaQueryWrapper<>();
        query.eq(AppUser::getUserId, userId);
        int n = appUserMapper.delete(query);
        logger.info("delete app user num[{}] for user[{}]", n, userId);
    }

    @Override
    public Pager<AppUser> search(AppUserSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new AppUserSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(AppUser.class, searchRequest);
        QueryWrapper<AppUser> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (!StringUtils.isEmpty(searchRequest.appId)) query.eq("app_id", searchRequest.appId);
        if (!StringUtils.isEmpty(searchRequest.userId)) query.eq("user_id", searchRequest.userId);
        IPage<AppUser> page = appUserMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
