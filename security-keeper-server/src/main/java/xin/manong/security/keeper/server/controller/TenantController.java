package xin.manong.security.keeper.server.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.Tenant;
import xin.manong.security.keeper.model.Vendor;
import xin.manong.security.keeper.server.converter.Converter;
import xin.manong.security.keeper.server.response.ViewTenant;
import xin.manong.security.keeper.server.service.TenantService;
import xin.manong.security.keeper.server.service.VendorService;
import xin.manong.security.keeper.server.service.request.TenantSearchRequest;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

/**
 * 租户控制器
 *
 * @author frankcl
 * @date 2023-09-05 11:44:05
 */
@RestController
@Controller
@Path("/tenant")
@RequestMapping("/tenant")
public class TenantController {

    private static final Logger logger = LoggerFactory.getLogger(TenantController.class);

    @Resource
    protected TenantService tenantService;
    @Resource
    protected VendorService vendorService;

    /**
     * 获取租户信息
     *
     * @param id 租户ID
     * @return 租户信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    public ViewTenant get(@QueryParam("id")  String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("tenant id is empty");
            throw new BadRequestException("租户ID为空");
        }
        Tenant tenant = tenantService.get(id);
        if (tenant == null) {
            logger.error("tenant[{}] is not found", id);
            throw new NotFoundException(String.format("租户[%s]不存在", id));
        }
        return fillAndConvertTenant(tenant);
    }

    /**
     * 增加租户信息
     *
     * @param tenant 租户信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PostMapping("add")
    public boolean add(@RequestBody Tenant tenant) {
        if (tenant == null) {
            logger.error("add tenant is null");
            throw new BadRequestException("增加租户信息为空");
        }
        tenant.check();
        return tenantService.add(tenant);
    }

    /**
     * 更新租户信息
     *
     * @param tenant 租户信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    public boolean update(@RequestBody Tenant tenant) {
        if (tenant == null) {
            logger.error("update tenant is null");
            throw new BadRequestException("更新租户信息为空");
        }
        if (StringUtils.isEmpty(tenant.id)) {
            logger.error("tenant id is empty");
            throw new BadRequestException("租户ID为空");
        }
        return tenantService.update(tenant);
    }

    /**
     * 删除租户信息
     *
     * @param id 租户ID
     * @return 删除成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @DeleteMapping("delete")
    public boolean delete(@QueryParam("id") String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("tenant id is empty");
            throw new BadRequestException("租户ID为空");
        }
        return tenantService.delete(id);
    }

    /**
     * 搜索租户
     *
     * @param searchRequest 搜索请求
     * @return 租户分页列表
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    @PostMapping("search")
    public Pager<ViewTenant> search(@RequestBody TenantSearchRequest searchRequest) {
        Pager<Tenant> pager = tenantService.search(searchRequest);
        Pager<ViewTenant> viewPager = new Pager<>();
        viewPager.current = pager.current;
        viewPager.size = pager.size;
        viewPager.total = pager.total;
        viewPager.records = new ArrayList<>();
        for (Tenant tenant : pager.records) {
            ViewTenant viewTenant = fillAndConvertTenant(tenant);
            viewPager.records.add(viewTenant);
        }
        return viewPager;
    }

    /**
     * 填充并转换视图层租户信息
     *
     * @param tenant 租户信息
     * @return 视图层租户信息
     */
    private ViewTenant fillAndConvertTenant(Tenant tenant) {
        Vendor vendor = vendorService.get(tenant.vendorId);
        if (vendor == null) {
            logger.error("vendor[{}] is not found", tenant.vendorId);
            throw new NotFoundException(String.format("供应商[%s]不存在", tenant.vendorId));
        }
        ViewTenant viewTenant = Converter.convert(tenant, vendor);
        if (viewTenant == null) {
            logger.error("convert view tenant failed");
            throw new RuntimeException("转换视图层租户信息失败");
        }
        return viewTenant;
    }
}
