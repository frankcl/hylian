package xin.manong.hylian.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.ws.rs.BadRequestException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.model.Permission;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.dao.mapper.PermissionMapper;
import xin.manong.hylian.server.service.PermissionService;
import xin.manong.hylian.server.service.RolePermissionService;
import xin.manong.hylian.server.service.request.PermissionSearchRequest;
import xin.manong.hylian.server.util.ModelValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 权限服务实现
 *
 * @author frankcl
 * @date 2023-10-13 14:15:24
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

    @Resource
    protected PermissionMapper permissionMapper;
    @Lazy
    @Resource
    protected RolePermissionService rolePermissionService;

    @Override
    public Permission get(String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("权限ID为空");
        return permissionMapper.selectById(id);
    }

    @Override
    public List<Permission> batchGet(List<String> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        List<Permission> permissions = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch countDownLatch = new CountDownLatch(ids.size());
        ids.stream().parallel().forEach(id -> {
            try {
                Permission permission = permissionMapper.selectById(id);
                if (permission != null) permissions.add(permission);
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            } finally {
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return new ArrayList<>(permissions);
    }

    @Override
    public boolean add(Permission permission) {
        beforeAddUpdate(permission);
        return permissionMapper.insert(permission) > 0;
    }

    @Override
    public boolean update(Permission permission) {
        beforeAddUpdate(permission);
        return permissionMapper.updateById(permission) > 0;
    }

    @Override
    public boolean delete(String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("权限ID为空");
        boolean success = permissionMapper.deleteById(id) > 0;
        if (success) rolePermissionService.deleteByPermission(id);
        return success;
    }

    @Override
    public void deleteByApp(String appId) {
        if (StringUtils.isEmpty(appId)) throw new BadRequestException("应用ID为空");
        LambdaQueryWrapper<Permission> query = new LambdaQueryWrapper<>();
        query.eq(Permission::getAppId, appId);
        int n = permissionMapper.delete(query);
        logger.info("delete permission num[{}] for app[{}]", n, appId);
    }

    @Override
    public Pager<Permission> search(PermissionSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new PermissionSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(Permission.class, searchRequest);
        QueryWrapper<Permission> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (!StringUtils.isEmpty(searchRequest.path)) query.like("path", searchRequest.path);
        if (!StringUtils.isEmpty(searchRequest.name)) query.like("name", searchRequest.name);
        if (!StringUtils.isEmpty(searchRequest.appId)) query.eq("app_id", searchRequest.appId);
        if (searchRequest.appIds != null) query.in("app_id", searchRequest.appIds);
        IPage<Permission> page = permissionMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }

    /**
     * 添加更新前置检查
     *
     * @param permission 权限
     */
    private void beforeAddUpdate(Permission permission) {
        LambdaQueryWrapper<Permission> query = new LambdaQueryWrapper<>();
        query.eq(Permission::getAppId, permission.appId);
        query.and(wrapper -> {
            wrapper.eq(Permission::getPath, permission.path);
            wrapper.or().eq(Permission::getName, permission.name);
        });
        if (StringUtils.isNotEmpty(permission.id)) query.ne(Permission::getId, permission.id);
        if (permissionMapper.selectCount(query) > 0) throw new IllegalStateException("权限已存在");
    }
}
