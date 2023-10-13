package xin.manong.security.keeper.server.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.Permission;
import xin.manong.security.keeper.server.service.PermissionService;
import xin.manong.security.keeper.server.service.request.PermissionSearchRequest;
import xin.manong.weapon.base.util.RandomID;
import xin.manong.weapon.spring.web.ws.aspect.EnableWebLogAspect;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 权限控制器
 *
 * @author frankcl
 * @date 2023-10-13 16:38:32
 */
@RestController
@Controller
@Path("/permission")
@RequestMapping("/permission")
public class PermissionController {

    private static final Logger logger = LoggerFactory.getLogger(PermissionController.class);

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
    @EnableWebLogAspect
    public Permission get(@QueryParam("id") @RequestParam("id") String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("permission id is empty");
            throw new BadRequestException("权限ID为空");
        }
        Permission permission = permissionService.get(id);
        if (permission == null) {
            logger.error("permission[{}] is not found", id);
            throw new NotFoundException(String.format("权限[%s]不存在", id));
        }
        return permission;
    }

    /**
     * 增加权限信息
     *
     * @param permission 权限信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PostMapping("add")
    @EnableWebLogAspect
    public boolean add(@RequestBody Permission permission) {
        if (permission == null) {
            logger.error("add permission is null");
            throw new BadRequestException("增加权限信息为空");
        }
        permission.id = RandomID.build();
        permission.check();
        return permissionService.add(permission);
    }

    /**
     * 更新权限信息
     *
     * @param permission 权限信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    @EnableWebLogAspect
    public boolean update(@RequestBody Permission permission) {
        if (permission == null || StringUtils.isEmpty(permission.id)) {
            logger.error("update permission or id is null");
            throw new BadRequestException("更新权限信息或ID为空");
        }
        return permissionService.update(permission);
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
    public boolean delete(@QueryParam("id") @RequestParam("id") String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("permission id is empty");
            throw new BadRequestException("权限ID为空");
        }
        return permissionService.delete(id);
    }

    /**
     * 搜索权限
     *
     * @param searchRequest 搜索请求
     * @return 权限分页列表
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    @PostMapping("search")
    @EnableWebLogAspect
    public Pager<Permission> search(@RequestBody PermissionSearchRequest searchRequest) {
        return permissionService.search(searchRequest);
    }
}
