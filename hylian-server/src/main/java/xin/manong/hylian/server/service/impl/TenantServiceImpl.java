package xin.manong.hylian.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import xin.manong.hylian.server.service.request.UserSearchRequest;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.dao.mapper.TenantMapper;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.model.Tenant;
import xin.manong.hylian.model.User;
import xin.manong.hylian.server.service.TenantService;
import xin.manong.hylian.server.service.request.TenantSearchRequest;
import xin.manong.hylian.server.service.UserService;
import xin.manong.hylian.server.util.ModelValidator;

import java.util.List;

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
        if (StringUtils.isEmpty(id)) throw new BadRequestException("租户ID为空");
        return tenantMapper.selectById(id);
    }

    @Override
    public boolean add(Tenant tenant) {
        LambdaQueryWrapper<Tenant> query = new LambdaQueryWrapper<>();
        query.eq(Tenant::getId, tenant.id);
        query.or(wrapper -> wrapper.eq(Tenant::getName, tenant.name));
        if (tenantMapper.selectCount(query) > 0) throw new IllegalStateException("租户已存在");
        return tenantMapper.insert(tenant) > 0;
    }

    @Override
    public boolean update(Tenant tenant) {
        Tenant prevTenant = tenantMapper.selectById(tenant.id);
        if (prevTenant == null) throw new NotFoundException("租户不存在");
        if (StringUtils.isEmpty(tenant.name)) tenant.name = null;
        String name = tenant.name != null && !tenant.name.equals(prevTenant.name) ?
                tenant.name : prevTenant.name;
        if (!name.equals(prevTenant.name)) {
            LambdaQueryWrapper<Tenant> query = new LambdaQueryWrapper<>();
            query.eq(Tenant::getName, name);
            if (tenantMapper.selectCount(query) > 0) throw new IllegalStateException("租户已存在");
        }
        return tenantMapper.updateById(tenant) > 0;
    }

    @Override
    public boolean delete(String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("租户ID为空");
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.tenantId = id;
        Pager<User> pager = userService.search(searchRequest);
        if (pager != null && pager.total > 0) {
            logger.error("users are found for tenant[{}], not allowed to be deleted", id);
            throw new ForbiddenException("该租户下存在用户，不能删除");
        }
        return tenantMapper.deleteById(id) > 0;
    }

    @Override
    public List<Tenant> getTenants() {
        return tenantMapper.selectList(null);
    }

    @Override
    public Pager<Tenant> search(TenantSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new TenantSearchRequest();
        if (searchRequest.pageNum == null || searchRequest.pageNum < 1) searchRequest.pageNum = Constants.DEFAULT_PAGE_NUM;
        if (searchRequest.pageSize == null || searchRequest.pageSize <= 0) searchRequest.pageSize = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(Tenant.class, searchRequest);
        QueryWrapper<Tenant> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (!StringUtils.isEmpty(searchRequest.name)) query.like("name", searchRequest.name);
        IPage<Tenant> page = tenantMapper.selectPage(new Page<>(searchRequest.pageNum, searchRequest.pageSize), query);
        return Converter.convert(page);
    }
}
