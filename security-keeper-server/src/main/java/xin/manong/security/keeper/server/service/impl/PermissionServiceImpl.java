package xin.manong.security.keeper.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.Permission;
import xin.manong.security.keeper.server.common.Constants;
import xin.manong.security.keeper.server.converter.Converter;
import xin.manong.security.keeper.server.dao.mapper.PermissionMapper;
import xin.manong.security.keeper.server.service.PermissionService;
import xin.manong.security.keeper.server.service.request.PermissionSearchRequest;

import javax.annotation.Resource;
import javax.ws.rs.NotFoundException;
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

    @Override
    public Permission get(String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("permission id is empty");
            throw new RuntimeException("权限ID为空");
        }
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
                logger.error("exception occurred for getting permission[{}]", id);
                logger.error(e.getMessage(), e);
            } finally {
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<>(permissions);
    }

    @Override
    public boolean add(Permission permission) {
        LambdaQueryWrapper<Permission> query = new LambdaQueryWrapper<>();
        query.eq(Permission::getAppId, permission.appId);
        query.and(wrapper -> {
            wrapper.eq(Permission::getResource, permission.resource);
            wrapper.or().eq(Permission::getName, permission.name);
        });
        query.eq(Permission::getResource, permission.resource).or().eq(Permission::getName, permission.name);
        if (permissionMapper.selectCount(query) > 0) {
            logger.error("permission has existed for the same name[{}] or resource[{}]",
                    permission.name, permission.resource);
            throw new RuntimeException(String.format("同名[%s]或相同访问资源[%s]权限存在",
                    permission.name, permission.resource));
        }
        return permissionMapper.insert(permission) > 0;
    }

    @Override
    public boolean update(Permission permission) {
        if (permissionMapper.selectById(permission.id) == null) {
            logger.error("permission is not found for id[{}]", permission.id);
            throw new NotFoundException(String.format("权限[%s]不存在", permission.id));
        }
        return permissionMapper.updateById(permission) > 0;
    }

    @Override
    public boolean delete(String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("permission id is empty");
            throw new RuntimeException("权限ID为空");
        }
        return permissionMapper.deleteById(id) > 0;
    }

    @Override
    public Pager<Permission> search(PermissionSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new PermissionSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        LambdaQueryWrapper<Permission> query = new LambdaQueryWrapper<>();
        query.orderByDesc(Permission::getCreateTime);
        if (!StringUtils.isEmpty(searchRequest.resource)) query.like(Permission::getResource, searchRequest.resource);
        if (!StringUtils.isEmpty(searchRequest.name)) query.like(Permission::getName, searchRequest.name);
        if (!StringUtils.isEmpty(searchRequest.appId)) query.eq(Permission::getAppId, searchRequest.appId);
        IPage<Permission> page = permissionMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
