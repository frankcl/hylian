package xin.manong.hylian.server.controller;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.hylian.model.*;
import xin.manong.hylian.server.response.ViewTenant;
import xin.manong.hylian.server.response.ViewUser;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.request.PasswordChangeRequest;
import xin.manong.hylian.server.request.UserRequest;
import xin.manong.hylian.server.request.UserRoleRequest;
import xin.manong.hylian.server.request.UserUpdateRequest;
import xin.manong.hylian.server.service.TenantService;
import xin.manong.hylian.server.service.UserRoleService;
import xin.manong.hylian.server.service.UserService;
import xin.manong.hylian.server.service.VendorService;
import xin.manong.hylian.server.service.request.UserSearchRequest;
import xin.manong.hylian.client.core.ContextManager;
import xin.manong.weapon.base.util.RandomID;
import xin.manong.weapon.spring.web.ws.aspect.EnableWebLogAspect;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

/**
 * 用户控制器
 *
 * @author frankcl
 * @date 2023-09-05 10:43:03
 */
@RestController
@Controller
@Path("api/user")
@RequestMapping("api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Resource
    protected UserService userService;
    @Resource
    protected TenantService tenantService;
    @Resource
    protected VendorService vendorService;
    @Resource
    protected UserRoleService userRoleService;

    /**
     * 获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    @EnableWebLogAspect
    public ViewUser get(@QueryParam("id") @RequestParam("id") String id) {
        User user = userService.get(id);
        if (user == null) {
            logger.error("user[{}] is not found", id);
            throw new NotFoundException("用户不存在");
        }
        return fillAndConvertUser(user);
    }

    /**
     * 增加用户信息
     *
     * @param userRequest 用户信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PostMapping("add")
    @EnableWebLogAspect
    public boolean add(@RequestBody UserRequest userRequest) {
        if (userRequest == null) throw new BadRequestException("用户信息为空");
        userRequest.check();
        User user = Converter.convert(userRequest);
        user.id = RandomID.build();
        Tenant tenant = tenantService.get(user.tenantId);
        if (tenant == null) {
            logger.error("tenant[{}] is not found for adding", user.tenantId);
            throw new NotFoundException("租户不存在");
        }
        user.vendorId = tenant.vendorId;
        user.check();
        return userService.add(user);
    }

    /**
     * 更新用户信息
     *
     * @param userUpdateRequest 用户信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    @EnableWebLogAspect
    public boolean update(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null) throw new BadRequestException("用户信息为空");
        userUpdateRequest.check();
        User user = Converter.convert(userUpdateRequest);
        if (!StringUtils.isEmpty(user.tenantId)) {
            Tenant tenant = tenantService.get(user.tenantId);
            if (tenant == null) {
                logger.error("tenant[{}] is not found for updating", user.tenantId);
                throw new NotFoundException("租户不存在");
            }
            user.vendorId = tenant.vendorId;
        }
        return userService.update(user);
    }

    /**
     * 删除用户信息
     *
     * @param id 用户ID
     * @return 删除成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @DeleteMapping("delete")
    @EnableWebLogAspect
    public boolean delete(@QueryParam("id") @RequestParam("id") String id) {
        return userService.delete(id);
    }

    /**
     * 添加用户角色关系
     *
     * @param request 用户角色关系
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("addUserRole")
    @PostMapping("addUserRole")
    @EnableWebLogAspect
    public boolean addUserRole(@RequestBody UserRoleRequest request) {
        if (request == null) throw new BadRequestException("用户角色关系为空");
        request.check();
        UserRole userRole = Converter.convert(request);
        userRole.check();
        return userRoleService.add(userRole);
    }

    /**
     * 删除用户角色关系
     *
     * @param id 用户角色关系ID
     * @return 成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("removeUserRole")
    @DeleteMapping("removeUserRole")
    @EnableWebLogAspect
    public boolean removeUserRole(@QueryParam("id") @RequestParam("id") Long id) {
        return userRoleService.delete(id);
    }

    /**
     * 搜索用户
     *
     * @param searchRequest 搜索请求
     * @return 用户分页列表
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    @PostMapping("search")
    @EnableWebLogAspect
    public Pager<ViewUser> search(@RequestBody UserSearchRequest searchRequest) {
        Pager<User> pager = userService.search(searchRequest);
        Pager<ViewUser> viewPager = new Pager<>();
        viewPager.current = pager.current;
        viewPager.size = pager.size;
        viewPager.total = pager.total;
        viewPager.records = new ArrayList<>();
        for (User user : pager.records) {
            ViewUser viewUser = fillAndConvertUser(user);
            viewPager.records.add(viewUser);
        }
        return viewPager;
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getCurrentUser")
    @GetMapping("getCurrentUser")
    @EnableWebLogAspect
    public ViewUser getCurrentUser() {
        User user = ContextManager.getUser();
        Tenant tenant = ContextManager.getTenant();
        Vendor vendor = ContextManager.getVendor();
        ViewTenant viewTenant = Converter.convert(tenant, vendor);
        return Converter.convert(user, viewTenant);
    }

    /**
     * 修改密码
     *
     * @param request 修改密码请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("changePassword")
    @PostMapping("changePassword")
    @EnableWebLogAspect
    public boolean changePassword(@RequestBody PasswordChangeRequest request) {
        if (request == null) throw new BadRequestException("修改密码请求为空");
        request.check();
        User user = userService.getByUserName(request.userName);
        if (user == null) {
            logger.error("user is not found for username[{}]", request.userName);
            throw new NotFoundException("用户不存在");
        }
        if (!user.password.equals(DigestUtils.md5Hex(request.password))) {
            logger.error("password is not correct");
            throw new RuntimeException("用户密码不正确");
        }
        User updateUser = new User();
        updateUser.id = user.id;
        updateUser.password = request.newPassword.trim();
        return userService.update(updateUser);
    }

    /**
     * 填充并转换视图层用户信息
     *
     * @param user 用户信息
     * @return 视图层用户信息
     */
    private ViewUser fillAndConvertUser(User user) {
        Vendor vendor = vendorService.get(user.vendorId);
        if (vendor == null) {
            logger.error("vendor[{}] is not found", user.vendorId);
            throw new NotFoundException("供应商不存在");
        }
        Tenant tenant = tenantService.get(user.tenantId);
        if (tenant == null) {
            logger.error("tenant[{}] is not found", user.tenantId);
            throw new NotFoundException("租户不存在");
        }
        ViewTenant viewTenant = Converter.convert(tenant, vendor);
        return Converter.convert(user, viewTenant);
    }
}
