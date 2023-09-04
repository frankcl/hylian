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
import xin.manong.security.keeper.server.dao.mapper.VendorMapper;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.Tenant;
import xin.manong.security.keeper.model.Vendor;
import xin.manong.security.keeper.server.service.TenantService;
import xin.manong.security.keeper.server.service.request.TenantSearchRequest;
import xin.manong.security.keeper.server.service.request.VendorSearchRequest;
import xin.manong.security.keeper.server.service.VendorService;

import javax.annotation.Resource;

/**
 * 供应商服务实现
 *
 * @author frankcl
 * @date 2023-09-01 14:08:11
 */
@Service
public class VendorServiceImpl implements VendorService {

    private static final Logger logger = LoggerFactory.getLogger(VendorServiceImpl.class);

    @Resource
    protected VendorMapper vendorMapper;
    @Lazy
    @Resource
    protected TenantService tenantService;

    @Override
    public Vendor get(String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("vendor id is empty");
            throw new RuntimeException("供应商ID为空");
        }
        return vendorMapper.selectById(id);
    }

    @Override
    public boolean add(Vendor vendor) {
        LambdaQueryWrapper<Vendor> query = new LambdaQueryWrapper<>();
        query.eq(Vendor::getId, vendor.id).or().eq(Vendor::getName, vendor.name);
        if (vendorMapper.selectCount(query) > 0) {
            logger.error("vendor has existed for the same id[{}] or name[{}]", vendor.id, vendor.name);
            throw new RuntimeException(String.format("相同ID[%s]或名称[%s]供应商已存在", vendor.id, vendor.name));
        }
        return vendorMapper.insert(vendor) > 0;
    }

    @Override
    public boolean update(Vendor vendor) {
        Vendor prevVendor = vendorMapper.selectById(vendor.id);
        if (prevVendor == null) {
            logger.error("vendor is not found for id[{}]", vendor.id);
            throw new RuntimeException(String.format("供应商[%s]不存在", vendor.id));
        }
        if (StringUtils.isEmpty(vendor.name)) vendor.name = null;
        if (vendor.name != null && !vendor.name.equals(prevVendor.name)) {
            LambdaQueryWrapper<Vendor> query = new LambdaQueryWrapper<>();
            query.eq(Vendor::getName, vendor.name);
            if (vendorMapper.selectCount(query) > 0) {
                logger.error("vendor has existed for the same name[{}]", vendor.name);
                throw new RuntimeException(String.format("同名供应商[%s]已存在", vendor.name));
            }
        }
        return vendorMapper.updateById(vendor) > 0;
    }

    @Override
    public boolean delete(String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("vendor id is empty");
            throw new RuntimeException("供应商ID为空");
        }
        TenantSearchRequest searchRequest = new TenantSearchRequest();
        searchRequest.vendorId = id;
        Pager<Tenant> pager = tenantService.search(searchRequest);
        if (pager != null && pager.total > 0) {
            logger.error("tenants are found for vendor[{}], not allowed to be deleted", id);
            return false;
        }
        return vendorMapper.deleteById(id) > 0;
    }

    @Override
    public Pager<Vendor> search(VendorSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new VendorSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        LambdaQueryWrapper<Vendor> query = new LambdaQueryWrapper<>();
        query.orderByDesc(Vendor::getCreateTime);
        if (!StringUtils.isEmpty(searchRequest.name)) query.like(Vendor::getName, searchRequest.name);
        IPage<Vendor> page = vendorMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
