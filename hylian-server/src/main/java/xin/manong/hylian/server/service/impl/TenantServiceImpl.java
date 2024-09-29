package xin.manong.hylian.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import xin.manong.hylian.server.service.request.UserSearchRequest;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.dao.mapper.TenantMapper;
import xin.manong.hylian.model.Pager;
import xin.manong.hylian.model.Tenant;
import xin.manong.hylian.model.User;
import xin.manong.hylian.server.service.TenantService;
import xin.manong.hylian.server.service.request.TenantSearchRequest;
import xin.manong.hylian.server.service.UserService;

import javax.annotation.Resource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

/**
 * 租户服务实现
 *
 * @author frankcl
 * @date 2023-09-01 14:27:17
 */
@Service
public class TenantServiceImpl implements TenantService {

    private static final Logger logger = LoggerFactory.getLogger(TenantServiceImpl.class);

    @Resource
    protected TenantMapper tenantMapper;
    @Lazy
    @Resource
    protected UserService userService;

    @Override
    public Tenant get(String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("tenant id is empty for getting");
            throw new BadRequestException("租户ID为空");
        }
        return tenantMapper.selectById(id);
    }

    @Override
    public boolean add(Tenant tenant) {
        LambdaQueryWrapper<Tenant> query = new LambdaQueryWrapper<>();
        query.eq(Tenant::getId, tenant.id);
        query.or(wrapper -> wrapper.eq(Tenant::getName, tenant.name));
        if (tenantMapper.selectCount(query) > 0) {
            logger.error("the same name tenant has existed for adding");
            throw new IllegalStateException("租户已存在");
        }
        return tenantMapper.insert(tenant) > 0;
    }

    @Override
    public boolean update(Tenant tenant) {
        Tenant prevTenant = tenantMapper.selectById(tenant.id);
        if (prevTenant == null) {
            logger.error("tenant is not found for id[{}]", tenant.id);
            throw new NotFoundException("租户不存在");
        }
        if (StringUtils.isEmpty(tenant.name)) tenant.name = null;
        String name = tenant.name != null && !tenant.name.equals(prevTenant.name) ?
                tenant.name : prevTenant.name;
        if (!name.equals(prevTenant.name)) {
            LambdaQueryWrapper<Tenant> query = new LambdaQueryWrapper<>();
            query.eq(Tenant::getName, name);
            if (tenantMapper.selectCount(query) > 0) {
                logger.error("the same name tenant has existed for updating");
                throw new IllegalStateException("租户已存在");
            }
        }
        return tenantMapper.updateById(tenant) > 0;
    }

    @Override
    public boolean delete(String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("tenant id is empty for deleting");
            throw new BadRequestException("租户ID为空");
        }
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.tenantId = id;
        Pager<User> pager = userService.search(searchRequest);
        if (pager != null && pager.total > 0) {
            logger.error("users are found for tenant[{}], not allowed to be deleted", id);
            throw new IllegalStateException(String.format("租户尚存%d用户", pager.total));
        }
        return tenantMapper.deleteById(id) > 0;
    }

    @Override
    public Pager<Tenant> search(TenantSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new TenantSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        LambdaQueryWrapper<Tenant> query = new LambdaQueryWrapper<>();
        query.orderByDesc(Tenant::getCreateTime);
        if (!StringUtils.isEmpty(searchRequest.name)) query.like(Tenant::getName, searchRequest.name);
        IPage<Tenant> page = tenantMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
