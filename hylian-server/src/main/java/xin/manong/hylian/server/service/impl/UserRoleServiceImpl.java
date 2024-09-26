package xin.manong.hylian.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.hylian.model.Pager;
import xin.manong.hylian.model.UserRole;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.dao.mapper.UserRoleMapper;
import xin.manong.hylian.server.service.UserRoleService;
import xin.manong.hylian.server.service.request.UserRoleSearchRequest;

import javax.annotation.Resource;
import javax.ws.rs.BadRequestException;

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

    @Override
    public boolean add(UserRole userRole) {
        LambdaQueryWrapper<UserRole> query = new LambdaQueryWrapper<>();
        query.eq(UserRole::getUserId, userRole.userId).eq(UserRole::getRoleId, userRole.roleId);
        if (userRoleMapper.selectCount(query) > 0) {
            logger.error("user role has existed for user id[{}] and role id[{}]", userRole.userId, userRole.roleId);
            throw new IllegalStateException("用户角色关系已存在");
        }
        return userRoleMapper.insert(userRole) > 0;
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            logger.error("user role id is null");
            throw new BadRequestException("用户角色关系ID为空");
        }
        return userRoleMapper.deleteById(id) > 0;
    }

    @Override
    public Pager<UserRole> search(UserRoleSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new UserRoleSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        LambdaQueryWrapper<UserRole> query = new LambdaQueryWrapper<>();
        query.orderByDesc(UserRole::getCreateTime);
        if (!StringUtils.isEmpty(searchRequest.userId)) query.eq(UserRole::getUserId, searchRequest.userId);
        if (!StringUtils.isEmpty(searchRequest.roleId)) query.eq(UserRole::getRoleId, searchRequest.roleId);
        if (!StringUtils.isEmpty(searchRequest.appId)) query.eq(UserRole::getAppId, searchRequest.appId);
        IPage<UserRole> page = userRoleMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
