package xin.manong.hylian.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.hylian.model.Role;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.model.UserRole;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.dao.mapper.UserRoleMapper;
import xin.manong.hylian.server.service.RoleService;
import xin.manong.hylian.server.service.UserRoleService;
import xin.manong.hylian.server.service.request.UserRoleSearchRequest;
import xin.manong.hylian.server.util.ModelValidator;

import javax.annotation.Resource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色服务实现
 *
 * @author frankcl
 * @date 2023-10-16 11:38:06
 */
@Service
public class UserRoleServiceImpl implements UserRoleService {

    private static final Logger logger = LoggerFactory.getLogger(UserRoleServiceImpl.class);

    @Resource
    protected UserRoleMapper userRoleMapper;
    @Lazy
    @Resource
    protected RoleService roleService;

    @Override
    public UserRole get(Long id) {
        if (id == null) throw new BadRequestException("关系ID为空");
        return userRoleMapper.selectById(id);
    }

    @Override
    public boolean add(UserRole userRole) {
        LambdaQueryWrapper<UserRole> query = new LambdaQueryWrapper<>();
        query.eq(UserRole::getUserId, userRole.userId).eq(UserRole::getRoleId, userRole.roleId);
        if (userRoleMapper.selectCount(query) > 0) throw new IllegalStateException("用户角色关系已存在");
        return userRoleMapper.insert(userRole) > 0;
    }

    @Override
    public List<Role> getRolesByAppUser(String appId, String userId) {
        List<UserRole> userRoles = getByAppUser(appId, userId);
        List<String> roleIds = userRoles.stream().map(r -> r.roleId).collect(Collectors.toList());
        return roleService.batchGet(roleIds);
    }

    @Override
    public List<UserRole> getByAppUser(String appId, String userId) {
        if (StringUtils.isEmpty(appId)) throw new BadRequestException("应用ID为空");
        if (StringUtils.isEmpty(userId)) throw new BadRequestException("用户ID为空");
        LambdaQueryWrapper<UserRole> query = new LambdaQueryWrapper<>();
        query.eq(UserRole::getAppId, appId).eq(UserRole::getUserId, userId);
        return userRoleMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(List<UserRole> addUserRoles, List<Long> removeUserRoles) {
        for (UserRole userRole : addUserRoles) {
            if (!add(userRole)) throw new InternalServerErrorException("添加用户角色关系失败");
        }
        for (Long id : removeUserRoles) {
            if (!delete(id)) throw new InternalServerErrorException("删除用户角色关系失败");
        }
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) throw new BadRequestException("用户角色关系ID为空");
        return userRoleMapper.deleteById(id) > 0;
    }

    @Override
    public void deleteByUser(String userId) {
        if (StringUtils.isEmpty(userId)) throw new BadRequestException("用户ID为空");
        LambdaQueryWrapper<UserRole> query = new LambdaQueryWrapper<>();
        query.eq(UserRole::getUserId, userId);
        int n = userRoleMapper.delete(query);
        logger.info("delete user role relationship num[{}] for user[{}]", n, userId);
    }

    @Override
    public void deleteByRole(String roleId) {
        if (StringUtils.isEmpty(roleId)) throw new BadRequestException("角色ID为空");
        LambdaQueryWrapper<UserRole> query = new LambdaQueryWrapper<>();
        query.eq(UserRole::getRoleId, roleId);
        int n = userRoleMapper.delete(query);
        logger.info("delete user role relationship num[{}] for role[{}]", n, roleId);
    }

    @Override
    public Pager<UserRole> search(UserRoleSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new UserRoleSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(UserRole.class, searchRequest);
        QueryWrapper<UserRole> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (!StringUtils.isEmpty(searchRequest.userId)) query.eq("user_id", searchRequest.userId);
        if (!StringUtils.isEmpty(searchRequest.roleId)) query.eq("role_id", searchRequest.roleId);
        if (!StringUtils.isEmpty(searchRequest.appId)) query.eq("app_id", searchRequest.appId);
        IPage<UserRole> page = userRoleMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
