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
import org.springframework.transaction.annotation.Transactional;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.model.Role;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.dao.mapper.RoleMapper;
import xin.manong.hylian.server.service.RolePermissionService;
import xin.manong.hylian.server.service.RoleService;
import xin.manong.hylian.server.service.UserRoleService;
import xin.manong.hylian.server.service.request.RoleSearchRequest;
import xin.manong.hylian.server.util.ModelValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 角色服务实现
 *
 * @author frankcl
 * @date 2023-10-13 15:19:20
 */
@Service
public class RoleServiceImpl implements RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Resource
    protected RoleMapper roleMapper;
    @Lazy
    @Resource
    protected UserRoleService userRoleService;
    @Lazy
    @Resource
    protected RolePermissionService rolePermissionService;

    @Override
    public Role get(String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("角色ID为空");
        return roleMapper.selectById(id);
    }

    @Override
    public List<Role> batchGet(List<String> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        List<Role> roles = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch countDownLatch = new CountDownLatch(ids.size());
        ids.stream().parallel().forEach(id -> {
            try {
                Role role = roleMapper.selectById(id);
                if (role != null) roles.add(role);
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
        return new ArrayList<>(roles);
    }

    @Override
    public boolean add(Role role) {
        beforeAddUpdate(role);
        return roleMapper.insert(role) > 0;
    }

    @Override
    public boolean update(Role role) {
        beforeAddUpdate(role);
        return roleMapper.updateById(role) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("角色ID为空");
        boolean success = roleMapper.deleteById(id) > 0;
        if (success) {
            userRoleService.deleteByRole(id);
            rolePermissionService.deleteByRole(id);
        }
        return success;
    }

    @Override
    public void deleteByApp(String appId) {
        if (StringUtils.isEmpty(appId)) throw new BadRequestException("应用ID为空");
        LambdaQueryWrapper<Role> query = new LambdaQueryWrapper<>();
        query.eq(Role::getAppId, appId);
        int n = roleMapper.delete(query);
        logger.info("delete role num[{}] for app[{}]", n, appId);
    }

    @Override
    public List<Role> getAppRoles(String appId) {
        LambdaQueryWrapper<Role> query = new LambdaQueryWrapper<>();
        query.eq(Role::getAppId, appId);
        return roleMapper.selectList(query);
    }

    @Override
    public Pager<Role> search(RoleSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new RoleSearchRequest();
        if (searchRequest.pageNum == null || searchRequest.pageNum < 1) searchRequest.pageNum = Constants.DEFAULT_PAGE_NUM;
        if (searchRequest.pageSize == null || searchRequest.pageSize <= 0) searchRequest.pageSize = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(Role.class, searchRequest);
        QueryWrapper<Role> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (!StringUtils.isEmpty(searchRequest.name)) query.like("name", searchRequest.name);
        if (!StringUtils.isEmpty(searchRequest.appId)) query.eq("app_id", searchRequest.appId);
        if (searchRequest.appIds != null) query.in("app_id", searchRequest.appIds);
        IPage<Role> page = roleMapper.selectPage(new Page<>(searchRequest.pageNum, searchRequest.pageSize), query);
        return Converter.convert(page);
    }

    /**
     * 添加更新前检查
     *
     * @param role 角色
     */
    private void beforeAddUpdate(Role role) {
        LambdaQueryWrapper<Role> query = new LambdaQueryWrapper<>();
        query.eq(Role::getAppId, role.appId).eq(Role::getName, role.name);
        if (StringUtils.isNotEmpty(role.id)) query.ne(Role::getId, role.id);
        if (roleMapper.selectCount(query) > 0) {
            logger.error("role existed for name[{}]", role.name);
            throw new IllegalStateException("角色已存在");
        }
    }
}
