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
import xin.manong.hylian.model.Permission;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.model.RolePermission;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.dao.mapper.RolePermissionMapper;
import xin.manong.hylian.server.service.PermissionService;
import xin.manong.hylian.server.service.RolePermissionService;
import xin.manong.hylian.server.service.request.RolePermissionSearchRequest;
import xin.manong.hylian.server.util.ModelValidator;

import java.util.List;
import java.util.stream.Collectors;

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
    @Lazy
    @Resource
    protected PermissionService permissionService;

    @Override
    public RolePermission get(Long id) {
        if (id == null) throw new BadRequestException("角色权限关系ID为空");
        return rolePermissionMapper.selectById(id);
    }

    @Override
    public boolean add(RolePermission rolePermission) {
        LambdaQueryWrapper<RolePermission> query = new LambdaQueryWrapper<>();
        query.eq(RolePermission::getRoleId, rolePermission.roleId).
                eq(RolePermission::getPermissionId, rolePermission.permissionId);
        if (rolePermissionMapper.selectCount(query) > 0) throw new IllegalStateException("角色权限关系已存在");
        return rolePermissionMapper.insert(rolePermission) > 0;
    }

    @Override
    public List<RolePermission> getByRoleId(String roleId) {
        if (StringUtils.isEmpty(roleId)) throw new BadRequestException("角色ID为空");
        LambdaQueryWrapper<RolePermission> query = new LambdaQueryWrapper<>();
        query.eq(RolePermission::getRoleId, roleId);
        return rolePermissionMapper.selectList(query);
    }

    @Override
    public List<Permission> getPermissionsByRoleId(String roleId) {
        List<RolePermission> rolePermissions = getByRoleId(roleId);
        List<String> permissionIds = rolePermissions.stream().map(r -> r.permissionId).collect(Collectors.toList());
        return permissionService.batchGet(permissionIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(List<RolePermission> addRolePermissions, List<Long> removeRolePermissions) {
        for (RolePermission rolePermission : addRolePermissions) {
            if (!add(rolePermission)) throw new InternalServerErrorException("添加角色权限关系失败");
        }
        for (Long id : removeRolePermissions) {
            if (!delete(id)) throw new InternalServerErrorException("删除角色权限关系失败");
        }
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) throw new BadRequestException("角色权限关系ID为空");
        return rolePermissionMapper.deleteById(id) > 0;
    }

    @Override
    public void deleteByRole(String roleId) {
        if (StringUtils.isEmpty(roleId)) throw new BadRequestException("角色ID为空");
        LambdaQueryWrapper<RolePermission> query = new LambdaQueryWrapper<>();
        query.eq(RolePermission::getRoleId, roleId);
        int n = rolePermissionMapper.delete(query);
        logger.info("Delete role permission relationship num:{} for role:{}", n, roleId);
    }

    @Override
    public void deleteByPermission(String permissionId) {
        if (StringUtils.isEmpty(permissionId)) throw new BadRequestException("权限ID为空");
        LambdaQueryWrapper<RolePermission> query = new LambdaQueryWrapper<>();
        query.eq(RolePermission::getPermissionId, permissionId);
        int n = rolePermissionMapper.delete(query);
        logger.info("Delete role permission relationship num:{} for permission:{}", n, permissionId);
    }

    @Override
    public Pager<RolePermission> search(RolePermissionSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new RolePermissionSearchRequest();
        if (searchRequest.pageNum == null || searchRequest.pageNum < 1) searchRequest.pageNum = Constants.DEFAULT_PAGE_NUM;
        if (searchRequest.pageSize == null || searchRequest.pageSize <= 0) searchRequest.pageSize = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(RolePermission.class, searchRequest);
        QueryWrapper<RolePermission> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (searchRequest.roleIds != null && !searchRequest.roleIds.isEmpty()) query.in("role_id", searchRequest.roleIds);
        if (!StringUtils.isEmpty(searchRequest.permissionId)) query.eq("permission_id", searchRequest.permissionId);
        IPage<RolePermission> page = rolePermissionMapper.selectPage(new Page<>(searchRequest.pageNum, searchRequest.pageSize), query);
        return Converter.convert(page);
    }
}
