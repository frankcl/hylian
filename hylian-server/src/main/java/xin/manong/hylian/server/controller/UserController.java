package xin.manong.hylian.server.controller;

import com.google.common.collect.Sets;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.hylian.client.aspect.EnableACLAspect;
import xin.manong.hylian.model.*;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.config.ServerConfig;
import xin.manong.hylian.server.controller.request.*;
import xin.manong.hylian.server.controller.response.ViewTenant;
import xin.manong.hylian.server.controller.response.ViewUser;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.server.service.TenantService;
import xin.manong.hylian.server.service.UserRoleService;
import xin.manong.hylian.server.service.UserService;
import xin.manong.hylian.server.service.request.UserSearchRequest;
import xin.manong.hylian.client.core.ContextManager;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;
import xin.manong.weapon.base.util.FileUtil;
import xin.manong.weapon.base.util.RandomID;
import xin.manong.weapon.spring.web.ws.aspect.EnableWebLogAspect;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    protected ServerConfig serverConfig;
    @Resource
    protected UserService userService;
    @Resource
    protected TenantService tenantService;
    @Resource
    protected UserRoleService userRoleService;
    @Resource
    protected OSSClient ossClient;

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
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    @EnableACLAspect
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
    @EnableACLAspect
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
    @EnableACLAspect
    @EnableWebLogAspect
    public boolean delete(@QueryParam("id") @RequestParam("id") String id) {
        User user = ContextManager.getUser();
        if (user != null && user.id.equals(id)) throw new UnsupportedOperationException("不能删除自己");
        return userService.delete(id);
    }

    /**
     * 添加用户角色关系
     *
     * @param request 用户角色关系
     * @return 成功返回true，否则返回false
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("addUserRole")
    @PutMapping("addUserRole")
    @EnableACLAspect
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
    @EnableACLAspect
    @EnableWebLogAspect
    public boolean removeUserRole(@QueryParam("id") @RequestParam("id") Long id) {
        return userRoleService.delete(id);
    }

    /**
     * 批量更新用户角色关系
     *
     * @param request 请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("batchUpdateUserRole")
    @PostMapping("batchUpdateUserRole")
    @EnableACLAspect
    @EnableWebLogAspect
    public boolean batchUpdateUserRole(@RequestBody BatchUserRoleRequest request) {
        if (request == null) throw new BadRequestException("批量更新请求为空");
        request.check();
        Set<UserRole> prevUserRoles = new HashSet<>(userRoleService.getByAppUser(request.appId, request.userId));
        Set<UserRole> currentUserRoles = new HashSet<>(Converter.convert(request));
        List<Long> removeUserRoles = Sets.difference(prevUserRoles, currentUserRoles).
                stream().map(r -> r.id).collect(Collectors.toList());
        List<UserRole> addUserRoles = new ArrayList<>(Sets.difference(currentUserRoles, prevUserRoles));
        userRoleService.batchUpdate(addUserRoles, removeUserRoles);
        return true;
    }

    /**
     * 获取应用用户角色列表
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @return 角色列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getAppUserRoles")
    @GetMapping("getAppUserRoles")
    @EnableWebLogAspect
    public List<Role> getAppUserRoles(@QueryParam("user_id") @RequestParam("user_id") String userId,
                                      @QueryParam("app_id") @RequestParam("app_id") String appId) {
        if (StringUtils.isEmpty(appId)) throw new BadRequestException("应用ID为空");
        if (StringUtils.isEmpty(userId)) throw new BadRequestException("用户ID为空");
        return userRoleService.getRolesByAppUser(appId, userId);
    }

    /**
     * 搜索用户
     *
     * @param searchRequest 搜索请求
     * @return 用户分页列表
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    @GetMapping("search")
    @EnableWebLogAspect
    public Pager<ViewUser> search(@BeanParam UserSearchRequest searchRequest) {
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
        ViewTenant viewTenant = Converter.convert(tenant);
        signAvatar(user);
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
        User user = userService.get(request.id);
        if (user == null) {
            logger.error("user is not found for id[{}]", request.id);
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
     * 上传头像
     *
     * @param fileDetail 文件信息
     * @param fileInputStream 文件流
     * @return 成功返回上传文件地址，否则抛出异常
     */
    @POST
    @Path("uploadAvatar")
    @PostMapping("uploadAvatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String uploadAvatar(@FormDataParam("file") FormDataContentDisposition fileDetail,
                               @FormDataParam("file") final InputStream fileInputStream) {
        String suffix = FileUtil.getFileSuffix(fileDetail.getFileName());
        String ossKey = String.format("%s%s%s", serverConfig.ossBaseDirectory,
                Constants.TEMP_AVATAR_DIR, RandomID.build());
        if (StringUtils.isNotEmpty(suffix)) ossKey = String.format("%s.%s", ossKey, suffix);
        if (!ossClient.putObject(serverConfig.ossBucket, ossKey, fileInputStream)) {
            logger.error("upload avatar failed");
            throw new IllegalStateException("上传头像失败");
        }
        return ossClient.sign(serverConfig.ossBucket, ossKey);
    }

    /**
     * 填充并转换视图层用户信息
     *
     * @param user 用户信息
     * @return 视图层用户信息
     */
    private ViewUser fillAndConvertUser(User user) {
        Tenant tenant = tenantService.get(user.tenantId);
        if (tenant == null) {
            logger.error("tenant[{}] is not found", user.tenantId);
            throw new NotFoundException("租户不存在");
        }
        ViewTenant viewTenant = Converter.convert(tenant);
        signAvatar(user);
        return Converter.convert(user, viewTenant);
    }

    /**
     * 加签头像地址
     *
     * @param user 用户信息
     */
    private void signAvatar(User user) {
        if (user == null || StringUtils.isEmpty(user.avatar)) return;
        OSSMeta ossMeta = OSSClient.parseURL(user.avatar);
        if (ossMeta == null) {
            logger.warn("avatar[{}] is invalid", user.avatar);
            return;
        }
        user.avatar = ossClient.sign(ossMeta.bucket, ossMeta.key);
    }
}
