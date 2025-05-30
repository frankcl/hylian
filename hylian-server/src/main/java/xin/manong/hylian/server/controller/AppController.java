package xin.manong.hylian.server.controller;

import com.google.common.collect.Sets;
import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.hylian.client.common.Constants;
import xin.manong.hylian.client.core.ContextManager;
import xin.manong.hylian.model.AppUser;
import xin.manong.hylian.model.User;
import xin.manong.hylian.server.aspect.EnableAppInjectAspect;
import xin.manong.hylian.server.controller.request.BatchAppUserRequest;
import xin.manong.hylian.server.controller.response.ViewUser;
import xin.manong.hylian.server.service.AppUserService;
import xin.manong.hylian.server.util.AppSecretUtils;
import xin.manong.hylian.model.App;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.controller.request.AppRequest;
import xin.manong.hylian.server.controller.request.AppUpdateRequest;
import xin.manong.hylian.server.service.AppService;
import xin.manong.hylian.server.service.request.AppSearchRequest;
import xin.manong.hylian.server.util.PermissionValidator;
import xin.manong.weapon.base.util.RandomID;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用控制器
 *
 * @author frankcl
 * @date 2023-09-05 11:58:49
 */
@RestController
@Controller
@Path("api/app")
@RequestMapping("api/app")
public class AppController {

    private static final int APP_SECRET_LEN = 8;

    @Resource
    private AppService appService;
    @Resource
    private AppUserService appUserService;

    /**
     * 获取应用信息
     *
     * @param id 应用ID
     * @return 应用信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    public App get(@QueryParam("id") @RequestParam("id") String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("应用ID为空");
        App app = appService.get(id);
        if (app == null) throw new NotFoundException("应用不存在");
        return app;
    }

    /**
     * 获取应用所有用户
     *
     * @param appId 应用ID
     * @return 用户列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getAppUsers")
    @GetMapping("getAppUsers")
    public List<ViewUser> getAppUsers(@QueryParam("app_id") @RequestParam("app_id") String appId) {
        if (StringUtils.isEmpty(appId)) throw new BadRequestException("应用ID为空");
        List<User> users = appUserService.getUsersByApp(appId);
        List<ViewUser> viewUsers = new ArrayList<>();
        users.forEach(user -> viewUsers.add(Converter.convert(user, null)));
        return viewUsers;
    }

    /**
     * 增加应用信息
     *
     * @param appRequest 应用信息
     * @return 成功返回true，否则返回false
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    @EnableWebLogAspect
    public boolean add(@RequestBody AppRequest appRequest) {
        User currentUser = ContextManager.getUser();
        assert currentUser != null;
        if (!currentUser.superAdmin) throw new ForbiddenException("无权操作");
        if (appRequest == null) throw new BadRequestException("应用信息为空");
        appRequest.check();
        App app = Converter.convert(appRequest);
        app.id = RandomID.build();
        app.check();
        return appService.add(app);
    }

    /**
     * 更新应用信息
     *
     * @param appRequest 应用信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    @EnableWebLogAspect
    @EnableAppInjectAspect
    public boolean update(@RequestBody AppUpdateRequest appRequest) {
        if (appRequest == null) throw new BadRequestException("应用信息为空");
        appRequest.check();
        PermissionValidator.validateAppPermission(appRequest.id);
        App app = Converter.convert(appRequest);
        return appService.update(app);
    }

    /**
     * 随机生成应用秘钥
     *
     * @return 应用秘钥
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("randomSecret")
    @GetMapping("randomSecret")
    public String randomSecret() {
        return AppSecretUtils.buildSecret(APP_SECRET_LEN);
    }

    /**
     * 删除应用信息
     *
     * @param id 应用ID
     * @return 删除成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @DeleteMapping("delete")
    @EnableWebLogAspect
    @EnableAppInjectAspect
    public boolean delete(@QueryParam("id") @RequestParam("id") String id) {
        PermissionValidator.validateAppPermission(id);
        return appService.delete(id);
    }

    /**
     * 批量更新应用用户关系
     * 1. 删除请求中不存在关系
     * 2. 添加请求中关系
     *
     * @param request 请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("batchUpdateAppUser")
    @PostMapping("batchUpdateAppUser")
    @EnableWebLogAspect
    @EnableAppInjectAspect
    public boolean batchUpdateAppUser(@RequestBody BatchAppUserRequest request) {
        if (request == null) throw new BadRequestException("批量更新请求为空");
        request.check();
        PermissionValidator.validateAppPermission(request.appId);
        Set<AppUser> prevAppUsers = new HashSet<>(appUserService.getByAppId(request.appId));
        Set<AppUser> currentAppUsers = new HashSet<>(Converter.convert(request));
        List<Long> removeAppUsers = new ArrayList<>(Sets.difference(
                prevAppUsers, currentAppUsers)).stream().map(r -> r.id).collect(Collectors.toList());
        List<AppUser> addAppUsers = new ArrayList<>(Sets.difference(
                currentAppUsers, prevAppUsers));
        appUserService.batchUpdate(addAppUsers, removeAppUsers);
        return true;
    }

    /**
     * 搜索应用
     *
     * @param searchRequest 搜索请求
     * @return 应用分页列表
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    @GetMapping("search")
    @EnableAppInjectAspect
    @SuppressWarnings("unchecked")
    public Pager<App> search(@BeanParam AppSearchRequest searchRequest) {
        User currentUser = ContextManager.getUser();
        assert currentUser != null;
        searchRequest.appIds = currentUser.superAdmin || searchRequest.ignoreCheck ?
                null : ContextManager.getValue(Constants.CURRENT_APPS, List.class);
        return searchRequest.appIds != null && searchRequest.appIds.isEmpty() ?
                Pager.empty(searchRequest.pageNum, searchRequest.pageSize) : appService.search(searchRequest);
    }

    /**
     * 获取所有应用
     *
     * @return 应用列表
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getApps")
    @GetMapping("getApps")
    @EnableAppInjectAspect
    @SuppressWarnings("unchecked")
    public List<App> getApps() {
        User currentUser = ContextManager.getUser();
        assert currentUser != null;
        List<App> apps = appService.getApps();
        List<String> myAppIds = currentUser.superAdmin ?
                null : ContextManager.getValue(Constants.CURRENT_APPS, List.class);
        if (myAppIds == null) return apps;
        return apps.stream().filter(app -> myAppIds.contains(app.id)).collect(Collectors.toList());
    }
}
