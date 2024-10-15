package xin.manong.hylian.server.controller;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.hylian.client.core.ContextManager;
import xin.manong.hylian.model.*;
import xin.manong.hylian.server.aspect.EnableAppFollowAspect;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.controller.request.BatchRolePermissionRequest;
import xin.manong.hylian.server.controller.request.RolePermissionRequest;
import xin.manong.hylian.server.controller.request.RoleRequest;
import xin.manong.hylian.server.controller.request.RoleUpdateRequest;
import xin.manong.hylian.server.controller.response.ViewRole;
import xin.manong.hylian.server.service.AppService;
import xin.manong.hylian.server.service.RolePermissionService;
import xin.manong.hylian.server.service.RoleService;
import xin.manong.hylian.server.service.request.RoleSearchRequest;
import xin.manong.hylian.server.util.PermissionValidator;
import xin.manong.weapon.base.util.RandomID;
import xin.manong.weapon.spring.web.ws.aspect.EnableWebLogAspect;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色控制器
 *
 * @author frankcl
 * @date 2023-10-13 16:47:25
 */
@RestController
@Controller
@Path("api/role")
@RequestMapping("api/role")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    @Resource
    protected AppService appService;
    @Resource
    protected RoleService roleService;
    @Resource
    protected RolePermissionService rolePermissionService;

    /**
     * 获取角色信息
     *
     * @param id 角色ID
     * @return 角色信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    @EnableWebLogAspect
    public Role get(@QueryParam("id") @RequestParam("id") String id) {
        Role role = roleService.get(id);
        if (role == null) {
            logger.error("role[{}] is not found", id);
            throw new NotFoundException("角色不存在");
        }
        return role;
    }

    /**
     * 增加角色信息
     *
     * @param roleRequest 角色信息
     * @return 成功返回true，否则返回false
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    @EnableWebLogAspect
    @EnableAppFollowAspect
    public boolean add(@RequestBody RoleRequest roleRequest) {
        if (roleRequest == null) throw new BadRequestException("角色信息为空");
        roleRequest.check();
        PermissionValidator.validateAppPermission(roleRequest.appId);
        Role role = Converter.convert(roleRequest);
        role.id = RandomID.build();
        role.check();
        return roleService.add(role);
    }

    /**
     * 更新角色信息
     *
     * @param roleUpdateRequest 更新角色信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    @EnableWebLogAspect
    @EnableAppFollowAspect
    public boolean update(@RequestBody RoleUpdateRequest roleUpdateRequest) {
        if (roleUpdateRequest == null) throw new BadRequestException("角色信息为空");
        roleUpdateRequest.check();
        if (roleUpdateRequest.appId != null) PermissionValidator.validateAppPermission(roleUpdateRequest.appId);
        Role role = roleService.get(roleUpdateRequest.id);
        if (role == null) throw new NotFoundException("角色不存在");
        PermissionValidator.validateAppPermission(role.appId);
        Role updateRole = Converter.convert(roleUpdateRequest);
        return roleService.update(updateRole);
    }

    /**
     * 增加角色权限关系
     *
     * @param request 角色权限关系
     * @return 成功返回true，否则返回false
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("addRolePermission")
    @PutMapping("addRolePermission")
    @EnableWebLogAspect
    @EnableAppFollowAspect
    public boolean addRolePermission(@RequestBody RolePermissionRequest request) {
        if (request == null) throw new BadRequestException("角色权限关系为空");
        request.check();
        Role role = roleService.get(request.roleId);
        if (role == null) throw new NotFoundException("角色不存在");
        PermissionValidator.validateAppPermission(role.appId);
        RolePermission rolePermission = Converter.convert(request);
        rolePermission.check();
        return rolePermissionService.add(rolePermission);
    }

    /**
     * 删除角色权限关系
     *
     * @param id 关系ID
     * @return 成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("removeRolePermission")
    @DeleteMapping("removeRolePermission")
    @EnableWebLogAspect
    @EnableAppFollowAspect
    public boolean removeRolePermission(@QueryParam("id") @RequestParam("id") Long id) {
        RolePermission rolePermission = rolePermissionService.get(id);
        if (rolePermission == null) throw new NotFoundException("角色权限关系不存在");
        Role role = roleService.get(rolePermission.roleId);
        if (role == null) throw new NotFoundException("角色不存在");
        PermissionValidator.validateAppPermission(role.appId);
        return rolePermissionService.delete(id);
    }

    /**
     * 批量更新角色权限关系
     * 1. 删除请求中不存在关系
     * 2. 添加请求中关系
     *
     * @param request 请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("batchUpdateRolePermission")
    @PostMapping("batchUpdateRolePermission")
    @EnableWebLogAspect
    @EnableAppFollowAspect
    public boolean batchUpdateRolePermission(@RequestBody BatchRolePermissionRequest request) {
        if (request == null) throw new BadRequestException("批量更新请求为空");
        request.check();
        Role role = roleService.get(request.roleId);
        if (role == null) throw new NotFoundException("角色不存在");
        PermissionValidator.validateAppPermission(role.appId);
        Set<RolePermission> prevRolePermissions = new HashSet<>(rolePermissionService.getByRoleId(request.roleId));
        Set<RolePermission> currentRolePermissions = new HashSet<>(Converter.convert(request));
        List<Long> removeRolePermissions = new ArrayList<>(Sets.difference(
                prevRolePermissions, currentRolePermissions)).stream().map(r -> r.id).collect(Collectors.toList());
        List<RolePermission> addRolePermissions = new ArrayList<>(Sets.difference(
                currentRolePermissions, prevRolePermissions));
        rolePermissionService.batchUpdate(addRolePermissions, removeRolePermissions);
        return true;
    }

    /**
     * 删除角色信息
     *
     * @param id 权限ID
     * @return 删除成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @DeleteMapping("delete")
    @EnableWebLogAspect
    @EnableAppFollowAspect
    public boolean delete(@QueryParam("id") @RequestParam("id") String id) {
        Role role = roleService.get(id);
        if (role == null) throw new NotFoundException("角色不存在");
        PermissionValidator.validateAppPermission(role.appId);
        return roleService.delete(id);
    }

    /**
     * 搜索角色
     *
     * @param searchRequest 搜索请求
     * @return 角色分页列表
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    @GetMapping("search")
    @EnableWebLogAspect
    @EnableAppFollowAspect
    public Pager<ViewRole> search(@BeanParam RoleSearchRequest searchRequest) {
        User currentUser = ContextManager.getUser();
        assert currentUser != null;
        searchRequest.appIds = currentUser.superAdmin ? null : ContextManager.getFollowApps();
        Pager<Role> pager = roleService.search(searchRequest);
        Pager<ViewRole> viewPager = new Pager<>();
        viewPager.current = pager.size;
        viewPager.size = pager.size;
        viewPager.total = pager.total;
        viewPager.records = new ArrayList<>();
        for (Role role : pager.records) viewPager.records.add(fillAndConvert(role));
        return viewPager;
    }

    /**
     * 获取角色权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getRolePermissions")
    @GetMapping("getRolePermissions")
    @EnableWebLogAspect
    public List<Permission> getRolePermissions(@QueryParam("role_id") String roleId) {
        if (StringUtils.isEmpty(roleId)) throw new BadRequestException("角色ID为空");
        return rolePermissionService.getPermissionsByRoleId(roleId);
    }

    /**
     * 填充转换视图层角色
     *
     * @param role 角色
     * @return 视图层角色
     */
    private ViewRole fillAndConvert(Role role) {
        App app = appService.get(role.getAppId());
        if (app == null) throw new NotFoundException("应用不存在");
        return Converter.convert(role, app);
    }
}
