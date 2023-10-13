package xin.manong.security.keeper.server.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.Role;
import xin.manong.security.keeper.server.request.RolePermissionRequest;
import xin.manong.security.keeper.server.service.RoleService;
import xin.manong.security.keeper.server.service.request.RoleSearchRequest;
import xin.manong.weapon.base.util.RandomID;
import xin.manong.weapon.spring.web.ws.aspect.EnableWebLogAspect;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 角色控制器
 *
 * @author frankcl
 * @date 2023-10-13 16:47:25
 */
@RestController
@Controller
@Path("/role")
@RequestMapping("/role")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    @Resource
    protected RoleService roleService;

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
        if (StringUtils.isEmpty(id)) {
            logger.error("role id is empty");
            throw new BadRequestException("角色ID为空");
        }
        Role role = roleService.get(id);
        if (role == null) {
            logger.error("role[{}] is not found", id);
            throw new NotFoundException(String.format("角色[%s]不存在", id));
        }
        return role;
    }

    /**
     * 增加角色信息
     *
     * @param role 角色信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PostMapping("add")
    @EnableWebLogAspect
    public boolean add(@RequestBody Role role) {
        if (role == null) {
            logger.error("add role is null");
            throw new BadRequestException("增加角色信息为空");
        }
        role.id = RandomID.build();
        role.check();
        return roleService.add(role);
    }

    /**
     * 更新角色信息
     *
     * @param role 角色信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    @EnableWebLogAspect
    public boolean update(@RequestBody Role role) {
        if (role == null || StringUtils.isEmpty(role.id)) {
            logger.error("update role or id is null");
            throw new BadRequestException("更新角色信息或ID为空");
        }
        return roleService.update(role);
    }

    /**
     * 增加权限
     *
     * @param request 角色权限请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("addPermission")
    @PostMapping("addPermission")
    @EnableWebLogAspect
    public boolean addPermission(@RequestBody RolePermissionRequest request) {
        if (request == null) {
            logger.error("role permission request is null");
            throw new BadRequestException("角色权限请求为空");
        }
        request.check();
        return roleService.addPermission(request.roleId, request.permissionId);
    }

    /**
     * 删除权限
     *
     * @param request 角色权限请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("removePermission")
    @PostMapping("removePermission")
    @EnableWebLogAspect
    public boolean removePermission(@RequestBody RolePermissionRequest request) {
        if (request == null) {
            logger.error("role permission request is null");
            throw new BadRequestException("角色权限请求为空");
        }
        request.check();
        return roleService.removePermission(request.roleId, request.permissionId);
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
    public boolean delete(@QueryParam("id") @RequestParam("id") String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("role id is empty");
            throw new BadRequestException("角色ID为空");
        }
        return roleService.delete(id);
    }

    /**
     * 搜索角色
     *
     * @param searchRequest 搜索请求
     * @return 角色分页列表
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    @PostMapping("search")
    @EnableWebLogAspect
    public Pager<Role> search(@RequestBody RoleSearchRequest searchRequest) {
        return roleService.search(searchRequest);
    }
}
