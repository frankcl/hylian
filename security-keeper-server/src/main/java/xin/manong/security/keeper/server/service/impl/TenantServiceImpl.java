package xin.manong.security.keeper.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import xin.manong.security.keeper.server.common.Constants;
import xin.manong.security.keeper.server.converter.Converter;
import xin.manong.security.keeper.server.dao.mapper.TenantMapper;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.Tenant;
import xin.manong.security.keeper.model.User;
import xin.manong.security.keeper.server.service.TenantService;
import xin.manong.security.keeper.server.service.request.TenantSearchRequest;
import xin.manong.security.keeper.server.service.UserService;
import xin.manong.security.keeper.server.service.VendorService;
import xin.manong.security.keeper.server.service.request.UserSearchRequest;

import javax.annotation.Resource;

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
    protected VendorService vendorService;
    @Lazy
    @Resource
    protected UserService userService;

    @Override
    public Tenant get(String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("tenant id is empty");
            throw new RuntimeException("租户ID为空");
        }
        return tenantMapper.selectById(id);
    }

    @Override
    public boolean add(Tenant tenant) {
        if (vendorService.get(tenant.vendorId) == null) {
            logger.error("vendor[{}] is not found", tenant.vendorId);
            throw new RuntimeException(String.format("供应商[%s]未找到", tenant.vendorId));
        }
        LambdaQueryWrapper<Tenant> query = new LambdaQueryWrapper<>();
        query.eq(Tenant::getId, tenant.id);
        query.or(wrapper -> wrapper.eq(Tenant::getVendorId, tenant.vendorId).eq(Tenant::getName, tenant.name));
        if (tenantMapper.selectCount(query) > 0) {
            logger.error("tenant has existed for the same id[{}] or name[{}] of vendor[{}]",
                    tenant.id, tenant.name, tenant.vendorId);
            throw new RuntimeException(String.format("相同ID[%s]或名称[%s]租户已存在",
                    tenant.id, tenant.name));
        }
        return tenantMapper.insert(tenant) > 0;
    }

    @Override
    public boolean update(Tenant tenant) {
        Tenant prevTenant = tenantMapper.selectById(tenant.id);
        if (prevTenant == null) {
            logger.error("tenant is not found for id[{}]", tenant.id);
            throw new RuntimeException(String.format("租户[%s]不存在", tenant.id));
        }
        if (StringUtils.isEmpty(tenant.name)) tenant.name = null;
        if (StringUtils.isEmpty(tenant.vendorId)) tenant.vendorId = null;
        String name = tenant.name != null && !tenant.name.equals(prevTenant.name) ?
                tenant.name : prevTenant.name;
        String vendorId = tenant.vendorId != null && !tenant.vendorId.equals(prevTenant.vendorId) ?
                tenant.vendorId : prevTenant.vendorId;
        if (!name.equals(prevTenant.name) || !vendorId.equals(prevTenant.vendorId)) {
            LambdaQueryWrapper<Tenant> query = new LambdaQueryWrapper<>();
            query.eq(Tenant::getVendorId, vendorId).eq(Tenant::getName, name);
            if (tenantMapper.selectCount(query) > 0) {
                logger.error("tenant has existed for the same name[{}] of vendor[{}]",
                        name, vendorId);
                throw new RuntimeException(String.format("供应商[%s]同名租户[%s]已存在",
                        vendorId, name));
            }
        }
        return tenantMapper.updateById(tenant) > 0;
    }

    @Override
    public boolean delete(String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("tenant id is empty");
            throw new RuntimeException("租户ID为空");
        }
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.tenantId = id;
        Pager<User> pager = userService.search(searchRequest);
        if (pager != null && pager.total > 0) {
            logger.error("users are found for tenant[{}], not allowed to be deleted", id);
            return false;
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
        if (!StringUtils.isEmpty(searchRequest.vendorId)) query.eq(Tenant::getVendorId, searchRequest.vendorId);
        IPage<Tenant> page = tenantMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
