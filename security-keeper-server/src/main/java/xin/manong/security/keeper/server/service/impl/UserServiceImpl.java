package xin.manong.security.keeper.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import xin.manong.security.keeper.model.view.request.UserSearchRequest;
import xin.manong.security.keeper.server.common.Constants;
import xin.manong.security.keeper.server.converter.Converter;
import xin.manong.security.keeper.server.dao.mapper.UserMapper;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.User;
import xin.manong.security.keeper.server.service.TenantService;
import xin.manong.security.keeper.server.service.UserService;
import xin.manong.security.keeper.server.service.VendorService;

import javax.annotation.Resource;

/**
 * 用户服务实现
 *
 * @author frankcl
 * @date 2023-09-01 13:40:43
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    protected UserMapper userMapper;
    @Lazy
    @Resource
    protected TenantService tenantService;
    @Lazy
    @Resource
    protected VendorService vendorService;

    @Override
    public User get(String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("user id is empty");
            throw new RuntimeException("用户ID为空");
        }
        return userMapper.selectById(id);
    }

    @Override
    public User getByUserName(String userName) {
        if (StringUtils.isEmpty(userName)) {
            logger.error("user name is empty");
            throw new RuntimeException("用户名为空");
        }
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.current = 1;
        searchRequest.size = 1;
        searchRequest.userName = userName;
        Pager<User> pager = search(searchRequest);
        if (pager == null || pager.total == 0 || pager.records.isEmpty()) return null;
        return pager.records.get(0);
    }

    @Override
    public boolean add(User user) {
        if (tenantService.get(user.tenantId) == null) {
            logger.error("tenant[{}] is not found", user.tenantId);
            throw new RuntimeException(String.format("租户[%s]不存在", user.tenantId));
        }
        if (vendorService.get(user.vendorId) == null) {
            logger.error("vendor[{}] is not found", user.vendorId);
            throw new RuntimeException(String.format("供应商[%s]不存在", user.vendorId));
        }
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getId, user.id).or().eq(User::getUserName, user.userName);
        if (userMapper.selectCount(query) > 0) {
            logger.error("user has existed for the same id[{}] or username[{}]", user.id, user.userName);
            throw new RuntimeException(String.format("同名用户ID[%s]或用户名[%s]已存在", user.id, user.userName));
        }
        user.password = DigestUtils.md5Hex(user.password.trim());
        return userMapper.insert(user) > 0;
    }

    @Override
    public boolean update(User user) {
        if (userMapper.selectById(user.id) == null) {
            logger.error("user is not found for id[{}]", user.id);
            throw new RuntimeException(String.format("用户[%s]不存在", user.id));
        }
        user.userName = null;
        if (user.password != null) user.password = DigestUtils.md5Hex(user.password);
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean delete(String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("user id is empty");
            throw new RuntimeException("用户ID为空");
        }
        return userMapper.deleteById(id) > 0;
    }

    @Override
    public Pager<User> search(UserSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new UserSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.orderByDesc(User::getCreateTime);
        if (!StringUtils.isEmpty(searchRequest.userName)) query.eq(User::getUserName, searchRequest.userName);
        if (!StringUtils.isEmpty(searchRequest.tenantId)) query.eq(User::getTenantId, searchRequest.tenantId);
        if (!StringUtils.isEmpty(searchRequest.vendorId)) query.eq(User::getVendorId, searchRequest.vendorId);
        if (!StringUtils.isEmpty(searchRequest.name)) query.like(User::getName, searchRequest.name);
        IPage<User> page = userMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
