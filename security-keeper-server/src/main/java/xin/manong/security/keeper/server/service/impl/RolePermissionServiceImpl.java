package xin.manong.security.keeper.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.RolePermission;
import xin.manong.security.keeper.server.common.Constants;
import xin.manong.security.keeper.server.converter.Converter;
import xin.manong.security.keeper.server.dao.mapper.RolePermissionMapper;
import xin.manong.security.keeper.server.service.RolePermissionService;
import xin.manong.security.keeper.server.service.request.RolePermissionSearchRequest;

import javax.annotation.Resource;

/**
 * 角色权限关系服务实现
 *
 * @author frankcl
 * @date 2023-10-16 11:44:52
 */
@Service
public class RolePermissionServiceImpl implements RolePermissionService {

    private static final Logger logger = LoggerFactory.getLogger(RolePermissionServiceImpl.class);

    @Resource
    protected RolePermissionMapper rolePermissionMapper;

    @Override
    public boolean add(RolePermission rolePermission) {
        LambdaQueryWrapper<RolePermission> query = new LambdaQueryWrapper<>();
        query.eq(RolePermission::getRoleId, rolePermission.roleId).
                eq(RolePermission::getPermissionId, rolePermission.permissionId);
        if (rolePermissionMapper.selectCount(query) > 0) {
            logger.error("role permission has existed for role id[{}] and permission id[{}]",
                    rolePermission.roleId, rolePermission.permissionId);
            throw new RuntimeException("角色权限关系已存在");
        }
        return rolePermissionMapper.insert(rolePermission) > 0;
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            logger.error("role permission id is null");
            throw new RuntimeException("角色权限关系ID为空");
        }
        return rolePermissionMapper.deleteById(id) > 0;
    }

    @Override
    public Pager<RolePermission> search(RolePermissionSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new RolePermissionSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        LambdaQueryWrapper<RolePermission> query = new LambdaQueryWrapper<>();
        query.orderByDesc(RolePermission::getCreateTime);
        if (searchRequest.roleIds != null && !searchRequest.roleIds.isEmpty()) query.in(RolePermission::getRoleId, searchRequest.roleIds);
        if (!StringUtils.isEmpty(searchRequest.permissionId)) query.eq(RolePermission::getPermissionId, searchRequest.permissionId);
        IPage<RolePermission> page = rolePermissionMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
