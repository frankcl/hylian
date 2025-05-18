package xin.manong.hylian.server.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.hylian.client.common.Constants;
import xin.manong.hylian.client.core.ContextManager;
import xin.manong.hylian.model.App;
import xin.manong.hylian.model.User;
import xin.manong.hylian.server.aspect.EnableAppInjectAspect;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.model.Permission;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.controller.request.PermissionRequest;
import xin.manong.hylian.server.controller.request.PermissionUpdateRequest;
import xin.manong.hylian.server.controller.response.ViewPermission;
import xin.manong.hylian.server.service.AppService;
import xin.manong.hylian.server.service.PermissionService;
import xin.manong.hylian.server.service.request.PermissionSearchRequest;
import xin.manong.hylian.server.util.PermissionValidator;
import xin.manong.weapon.base.util.RandomID;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限控制器
 *
 * @author frankcl
 * @date 2023-10-13 16:38:32
 */
@RestController
@Controller
@Path("api/permission")
@RequestMapping("api/permission")
public class PermissionController {

    @Resource
    protected AppService appService;
    @Resource
    protected PermissionService permissionService;

    /**
     * 获取权限信息
     *
     * @param id 权限ID
     * @return 权限信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    public Permission get(@QueryParam("id") @RequestParam("id") String id) {
        Permission permission = permissionService.get(id);
        if (permission == null) throw new NotFoundException("权限不存在");
        return permission;
    }

    /**
     * 批量获取权限
     *
     * @param ids 权限ID列表
     * @return 权限列表
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("batchGet")
    @PostMapping("batchGet")
    public List<Permission> batchGet(@RequestBody List<String> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        return permissionService.batchGet(ids);
    }

    /**
     * 增加权限信息
     *
     * @param permissionRequest 权限请求
     * @return 成功返回true，否则返回false
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    @EnableWebLogAspect
    @EnableAppInjectAspect
    public boolean add(@RequestBody PermissionRequest permissionRequest) {
        if (permissionRequest == null) throw new BadRequestException("权限信息为空");
        permissionRequest.check();
        PermissionValidator.validateAppPermission(permissionRequest.appId);
        Permission permission = Converter.convert(permissionRequest);
        permission.id = RandomID.build();
        permission.check();
        return permissionService.add(permission);
    }

    /**
     * 更新权限信息
     *
     * @param permissionUpdateRequest 权限更新信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    @EnableWebLogAspect
    @EnableAppInjectAspect
    public boolean update(@RequestBody PermissionUpdateRequest permissionUpdateRequest) {
        if (permissionUpdateRequest == null) throw new BadRequestException("权限信息为空");
        permissionUpdateRequest.check();
        if (permissionUpdateRequest.appId != null) {
            PermissionValidator.validateAppPermission(permissionUpdateRequest.appId);
        }
        Permission permission = permissionService.get(permissionUpdateRequest.id);
        if (permission == null) throw new NotFoundException("权限不存在");
        PermissionValidator.validateAppPermission(permission.appId);
        Permission updatePermission = Converter.convert(permissionUpdateRequest);
        return permissionService.update(updatePermission);
    }

    /**
     * 删除权限信息
     *
     * @param id 权限ID
     * @return 删除成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @DeleteMapping("delete")
    @EnableWebLogAspect
    @EnableAppInjectAspect
    public boolean delete(@QueryParam("id") @RequestParam("id") String id) {
        Permission permission = permissionService.get(id);
        if (permission == null) throw new NotFoundException("权限不存在");
        PermissionValidator.validateAppPermission(permission.appId);
        return permissionService.delete(id);
    }

    /**
     * 搜索权限
     *
     * @param searchRequest 搜索请求
     * @return 权限分页列表
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    @GetMapping("search")
    @EnableAppInjectAspect
    @SuppressWarnings("unchecked")
    public Pager<ViewPermission> search(@BeanParam PermissionSearchRequest searchRequest) {
        User currentUser = ContextManager.getUser();
        assert currentUser != null;
        searchRequest.appIds = currentUser.superAdmin || searchRequest.ignoreCheck ?
                null : ContextManager.getValue(Constants.CURRENT_APPS, List.class);
        Pager<Permission> pager = searchRequest.appIds != null && searchRequest.appIds.isEmpty() ?
                Pager.empty(searchRequest.pageNum, searchRequest.pageSize) : permissionService.search(searchRequest);
        Pager<ViewPermission> viewPager = new Pager<>();
        viewPager.pageNum = pager.pageNum;
        viewPager.pageSize = pager.pageSize;
        viewPager.total = pager.total;
        viewPager.records = new ArrayList<>();
        for (Permission permission : pager.records) viewPager.records.add(fillAndConvert(permission));
        return viewPager;
    }

    /**
     * 获取应用权限
     *
     * @param appId 应用ID
     * @return 权限列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getAppPermissions")
    @GetMapping("getAppPermissions")
    public List<ViewPermission> getAppPermissions(@QueryParam("app_id") String appId) {
        List<Permission> permissions = permissionService.getAppPermissions(appId);
        return permissions.stream().map(this::fillAndConvert).toList();
    }

    /**
     * 填充和转换权限
     *
     * @param permission 权限
     * @return 视图层权限
     */
    private ViewPermission fillAndConvert(Permission permission) {
        App app = appService.get(permission.appId);
        if (app == null) throw new NotFoundException("应用不存在");
        return Converter.convert(permission, app);
    }
}
