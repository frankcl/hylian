package xin.manong.security.keeper.server.controller;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.Tenant;
import xin.manong.security.keeper.model.User;
import xin.manong.security.keeper.model.Vendor;
import xin.manong.security.keeper.server.converter.Converter;
import xin.manong.security.keeper.server.request.PasswordRequest;
import xin.manong.security.keeper.server.response.ViewTenant;
import xin.manong.security.keeper.server.response.ViewUser;
import xin.manong.security.keeper.server.service.TenantService;
import xin.manong.security.keeper.server.service.UserService;
import xin.manong.security.keeper.server.service.VendorService;
import xin.manong.security.keeper.server.service.request.UserSearchRequest;
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
@Path("/user")
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Resource
    protected UserService userService;
    @Resource
    protected TenantService tenantService;
    @Resource
    protected VendorService vendorService;

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
        if (StringUtils.isEmpty(id)) {
            logger.error("user id is empty");
            throw new BadRequestException("用户ID为空");
        }
        User user = userService.get(id);
        if (user == null) {
            logger.error("user[{}] is not found", id);
            throw new NotFoundException(String.format("用户[%s]不存在", id));
        }
        return fillAndConvertUser(user);
    }

    /**
     * 增加用户信息
     *
     * @param user 用户信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PostMapping("add")
    @EnableWebLogAspect
    public boolean add(@RequestBody User user) {
        if (user == null) {
            logger.error("add user is null");
            throw new BadRequestException("增加用户信息为空");
        }
        user.check();
        return userService.add(user);
    }

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    @EnableWebLogAspect
    public boolean update(@RequestBody User user) {
        if (user == null) {
            logger.error("update user is null");
            throw new BadRequestException("更新用户信息为空");
        }
        if (StringUtils.isEmpty(user.id)) {
            logger.error("user id is empty");
            throw new BadRequestException("用户ID为空");
        }
        user.password = null;
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
        if (StringUtils.isEmpty(id)) {
            logger.error("user id is empty");
            throw new BadRequestException("用户ID为空");
        }
        return userService.delete(id);
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
    public boolean changePassword(@RequestBody PasswordRequest request) {
        if (request == null) {
            logger.error("change password request is null");
            throw new BadRequestException("修改密码请求为空");
        }
        request.check();
        User user = userService.getByUserName(request.userName);
        if (user == null) {
            logger.error("user is not found for username[{}]", request.userName);
            throw new NotFoundException(String.format("用户[%s]不存在", request.userName));
        }
        if (!user.password.equals(DigestUtils.md5Hex(request.password))) {
            logger.error("password is not correct");
            throw new RuntimeException("用户密码不正确");
        }
        User updateUser = new User();
        updateUser.id = user.id;
        updateUser.password = request.newPassword.trim();
        return userService.update(user);
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
            throw new NotFoundException(String.format("供应商[%s]不存在", user.vendorId));
        }
        Tenant tenant = tenantService.get(user.tenantId);
        if (tenant == null) {
            logger.error("tenant[{}] is not found", user.tenantId);
            throw new NotFoundException(String.format("租户[%s]不存在", user.tenantId));
        }
        ViewTenant viewTenant = Converter.convert(tenant, vendor);
        if (viewTenant == null) {
            logger.error("convert view tenant failed");
            throw new RuntimeException("转换视图层租户信息失败");
        }
        ViewUser viewUser = Converter.convert(user, viewTenant);
        if (viewUser == null) {
            logger.error("convert view user failed");
            throw new RuntimeException("转换视图层用户信息失败");
        }
        return viewUser;
    }
}
