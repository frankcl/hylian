package xin.manong.hylian.server.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.hylian.client.aspect.EnableACLAspect;
import xin.manong.hylian.client.core.ContextManager;
import xin.manong.hylian.client.util.SessionUtils;
import xin.manong.hylian.model.User;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.model.Tenant;
import xin.manong.hylian.server.controller.response.ViewTenant;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.controller.request.TenantRequest;
import xin.manong.hylian.server.controller.request.TenantUpdateRequest;
import xin.manong.hylian.server.service.TenantService;
import xin.manong.hylian.server.service.request.TenantSearchRequest;
import xin.manong.weapon.base.util.RandomID;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

import java.util.ArrayList;
import java.util.List;

/**
 * 租户控制器
 *
 * @author frankcl
 * @date 2023-09-05 11:44:05
 */
@RestController
@Controller
@Path("api/tenant")
@RequestMapping("api/tenant")
public class TenantController {

    @Resource
    private TenantService tenantService;

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
    public ViewTenant get(@QueryParam("id") @RequestParam("id") String id) {
        Tenant tenant = tenantService.get(id);
        if (tenant == null) throw new NotFoundException("租户不存在");
        return Converter.convert(tenant);
    }

    /**
     * 增加租户信息
     *
     * @param tenantRequest 租户信息
     * @return 成功返回true，否则返回false
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    @EnableACLAspect
    @EnableWebLogAspect
    public boolean add(@RequestBody TenantRequest tenantRequest) {
        User currentUser = ContextManager.getUser();
        assert currentUser != null;
        if (!currentUser.superAdmin) throw new ForbiddenException("无权操作");
        if (tenantRequest == null) throw new BadRequestException("租户信息为空");
        tenantRequest.check();
        Tenant tenant = Converter.convert(tenantRequest);
        tenant.id = RandomID.build();
        tenant.check();
        return tenantService.add(tenant);
    }

    /**
     * 更新租户信息
     *
     * @param tenantUpdateRequest 租户信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    @EnableACLAspect
    @EnableWebLogAspect
    public boolean update(@RequestBody TenantUpdateRequest tenantUpdateRequest,
                          @Context HttpServletRequest httpRequest) {
        User currentUser = ContextManager.getUser();
        assert currentUser != null;
        if (!currentUser.superAdmin) throw new ForbiddenException("无权操作");
        if (tenantUpdateRequest == null) throw new BadRequestException("更新租户信息为空");
        tenantUpdateRequest.check();
        Tenant tenant = Converter.convert(tenantUpdateRequest);
        if (currentUser.tenantId.equals(tenant.id)) SessionUtils.setRefreshUser(httpRequest);
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
    @EnableACLAspect
    @EnableWebLogAspect
    public boolean delete(@QueryParam("id") @RequestParam("id") String id) {
        User currentUser = ContextManager.getUser();
        assert currentUser != null;
        if (!currentUser.superAdmin) throw new ForbiddenException("无权操作");
        return tenantService.delete(id);
    }

    /**
     * 搜索租户
     *
     * @param searchRequest 搜索请求
     * @return 租户分页列表
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    @GetMapping("search")
    public Pager<ViewTenant> search(@BeanParam TenantSearchRequest searchRequest) {
        Pager<Tenant> pager = tenantService.search(searchRequest);
        Pager<ViewTenant> viewPager = new Pager<>();
        viewPager.pageNum = pager.pageNum;
        viewPager.pageSize = pager.pageSize;
        viewPager.total = pager.total;
        viewPager.records = new ArrayList<>();
        for (Tenant tenant : pager.records) {
            ViewTenant viewTenant = Converter.convert(tenant);
            viewPager.records.add(viewTenant);
        }
        return viewPager;
    }

    /**
     * 获取所有租户
     *
     * @return 租户列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getTenants")
    @GetMapping("getTenants")
    public List<Tenant> getTenants() {
        return tenantService.getTenants();
    }
}
