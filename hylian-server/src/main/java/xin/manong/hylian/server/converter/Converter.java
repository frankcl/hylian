package xin.manong.hylian.server.converter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.hylian.model.*;
import xin.manong.hylian.server.controller.request.*;
import xin.manong.hylian.server.controller.response.*;
import xin.manong.hylian.server.model.Pager;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据转换器
 *
 * @author frankcl
 * @date 2023-03-15 17:08:50
 */
public class Converter {

    private static final Logger logger = LoggerFactory.getLogger(Converter.class);

    /**
     * 转化数据库分页信息为通用分页信息
     *
     * @param page 数据库分页信息
     * @return 通用分页信息
     * @param <T> 数据类型
     */
    public static <T> Pager<T> convert(IPage<T> page) {
        if (page == null) return null;
        Pager<T> pager = new Pager<>();
        pager.records = page.getRecords();
        pager.current = page.getCurrent();
        pager.size = page.getSize();
        pager.total = page.getTotal();
        return pager;
    }

    /**
     * 转换注册信息为用户信息
     *
     * @param request 注册信息
     * @return 用户信息
     */
    public static User convert(RegisterRequest request) {
        if (request == null) return null;
        User user = new User();
        user.username = request.username;
        user.password = request.password;
        user.name = request.name;
        user.disabled = true;
        return user;
    }

    /**
     * 转换用户请求为用户对象
     *
     * @param userRequest 用户请求
     * @return 用户对象
     */
    public static User convert(UserRequest userRequest) {
        if (userRequest == null) return null;
        User user = new User();
        user.username = userRequest.username;
        user.password = userRequest.password;
        user.name = userRequest.name;
        user.tenantId = userRequest.tenantId;
        user.avatar = userRequest.avatar;
        user.disabled = userRequest.disabled == null || userRequest.disabled;
        return user;
    }

    /**
     * 转换用户更新请求为用户对象
     *
     * @param userRequest 用户更新请求
     * @return 用户对象
     */
    public static User convert(UserUpdateRequest userRequest) {
        if (userRequest == null) return null;
        User user = new User();
        user.id = userRequest.id;
        user.name = userRequest.name;
        user.tenantId = userRequest.tenantId;
        user.avatar = userRequest.avatar;
        user.disabled = userRequest.disabled;
        return user;
    }

    /**
     * 转换租户请求为租户对象
     *
     * @param tenantRequest 租户请求
     * @return 租户对象
     */
    public static Tenant convert(TenantRequest tenantRequest) {
        if (tenantRequest == null) return null;
        Tenant tenant = new Tenant();
        tenant.name = tenantRequest.name;
        return tenant;
    }

    /**
     * 转换租户更新请求为租户对象
     *
     * @param tenantRequest 租户更新请求
     * @return 租户对象
     */
    public static Tenant convert(TenantUpdateRequest tenantRequest) {
        if (tenantRequest == null) return null;
        Tenant tenant = new Tenant();
        tenant.id = tenantRequest.id;
        tenant.name = tenantRequest.name;
        return tenant;
    }

    /**
     * 转换用户角色关系请求为用户角色关系对象
     *
     * @param request 用户角色关系请求
     * @return 用户角色关系对象
     */
    public static UserRole convert(UserRoleRequest request) {
        if (request == null) return null;
        UserRole userRole = new UserRole();
        userRole.roleId = request.roleId;
        userRole.userId = request.userId;
        userRole.appId = request.appId;
        return userRole;
    }

    /**
     * 转换角色权限关系请求为角色权限关系对象
     *
     * @param request 角色权限关系请求
     * @return 角色权限关系对象
     */
    public static RolePermission convert(RolePermissionRequest request) {
        if (request == null) return null;
        RolePermission rolePermission = new RolePermission();
        rolePermission.roleId = request.roleId;
        rolePermission.permissionId = request.permissionId;
        return rolePermission;
    }

    /**
     * 转换应用请求为应用对象
     *
     * @param appRequest 应用请求
     * @return 应用对象
     */
    public static App convert(AppRequest appRequest) {
        if (appRequest == null) return null;
        App app = new App();
        app.name = appRequest.name;
        app.secret = appRequest.secret;
        app.description = appRequest.description;
        return app;
    }

    /**
     * 转换更新应用请求为应用对象
     *
     * @param appRequest 更新应用请求
     * @return 应用对象
     */
    public static App convert(AppUpdateRequest appRequest) {
        if (appRequest == null) return null;
        App app = new App();
        app.id = appRequest.id;
        app.name = appRequest.name;
        app.secret = appRequest.secret;
        app.description = appRequest.description;
        return app;
    }

    /**
     * 转换角色请求为角色对象
     *
     * @param roleRequest 角色请求
     * @return 角色对象
     */
    public static Role convert(RoleRequest roleRequest) {
        if (roleRequest == null) return null;
        Role role = new Role();
        role.name = roleRequest.name;
        role.appId = roleRequest.appId;
        return role;
    }

    /**
     * 转换角色更新请求为角色对象
     *
     * @param roleRequest 角色更新请求
     * @return 角色对象
     */
    public static Role convert(RoleUpdateRequest roleRequest) {
        if (roleRequest == null) return null;
        Role role = new Role();
        role.id = roleRequest.id;
        role.name = roleRequest.name;
        role.appId = roleRequest.appId;
        return role;
    }

    /**
     * 转换权限请求为权限对象
     *
     * @param permissionRequest 权限请求
     * @return 权限对象
     */
    public static Permission convert(PermissionRequest permissionRequest) {
        if (permissionRequest == null) return null;
        Permission permission = new Permission();
        permission.name = permissionRequest.name;
        permission.path = permissionRequest.path;
        permission.appId = permissionRequest.appId;
        return permission;
    }

    /**
     * 转换权限更新请求为权限对象
     *
     * @param permissionRequest 权限更新请求
     * @return 权限对象
     */
    public static Permission convert(PermissionUpdateRequest permissionRequest) {
        if (permissionRequest == null) return null;
        Permission permission = new Permission();
        permission.id = permissionRequest.id;
        permission.name = permissionRequest.name;
        permission.path = permissionRequest.path;
        permission.appId = permissionRequest.appId;
        return permission;
    }

    /**
     * 转换视图层租户信息
     *
     * @param tenant 租户信息
     * @return 视图层租户信息
     */
    public static ViewTenant convert(Tenant tenant) {
        if (tenant == null) return null;
        ViewTenant viewTenant = new ViewTenant();
        viewTenant.id = tenant.id;
        viewTenant.name = tenant.name;
        viewTenant.createTime = tenant.createTime;
        viewTenant.updateTime = tenant.updateTime;
        return viewTenant;
    }

    /**
     * 转换视图层用户信息
     *
     * @param user 用户信息
     * @param tenant 视图层租户信息
     * @return 视图层用户信息
     */
    public static ViewUser convert(User user, ViewTenant tenant) {
        if (user == null) return null;
        ViewUser viewUser = new ViewUser();
        viewUser.id = user.id;
        viewUser.username = user.username;
        viewUser.name = user.name;
        viewUser.avatar = user.avatar;
        viewUser.createTime = user.createTime;
        viewUser.updateTime = user.updateTime;
        viewUser.superAdmin = user.superAdmin;
        viewUser.disabled = user.disabled == null || user.disabled;
        viewUser.tenant = tenant;
        if (tenant != null) {
            if (!user.tenantId.equals(tenant.id)) {
                logger.error("tenants are not consistent for user[{}] and tenant[{}]", user.tenantId, tenant.id);
                throw new IllegalStateException("租户ID不一致");
            }
        }
        return viewUser;
    }

    /**
     * 转换视图层活动记录
     *
     * @param record 活动记录
     * @param app 所属应用
     * @param user 所属用户
     * @return 视图层活动记录
     */
    public static ViewActivity convert(Activity record, App app, ViewUser user) {
        ViewActivity viewActivity = new ViewActivity();
        viewActivity.id = record.id;
        viewActivity.sessionId = record.sessionId;
        viewActivity.createTime = record.createTime;
        viewActivity.updateTime = record.updateTime;
        viewActivity.app = app;
        viewActivity.user = user;
        return viewActivity;
    }

    /**
     * 转换视图层权限
     *
     * @param permission 权限
     * @param app 应用
     * @return 视图层权限
     */
    public static ViewPermission convert(Permission permission, App app) {
        ViewPermission viewPermission = new ViewPermission();
        viewPermission.id = permission.id;
        viewPermission.name = permission.name;
        viewPermission.path = permission.path;
        viewPermission.createTime = permission.createTime;
        viewPermission.updateTime = permission.updateTime;
        viewPermission.app = app;
        return viewPermission;
    }

    /**
     * 转换视图层角色
     *
     * @param role 角色
     * @param app 应用
     * @return 视图层角色
     */
    public static ViewRole convert(Role role, App app) {
        ViewRole viewRole = new ViewRole();
        viewRole.id = role.id;
        viewRole.name = role.name;
        viewRole.createTime = role.createTime;
        viewRole.updateTime = role.updateTime;
        viewRole.app = app;
        return viewRole;
    }

    /**
     * 转换批量更新请求为关系列表
     *
     * @param request 批量更新请求
     * @return 关系列表
     */
    public static List<RolePermission> convert(BatchRolePermissionRequest request) {
        List<RolePermission> rolePermissions = new ArrayList<>();
        if (request.permissionIds == null) return rolePermissions;
        for (String permissionId : request.permissionIds) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.permissionId = permissionId;
            rolePermission.roleId = request.roleId;
            rolePermissions.add(rolePermission);
        }
        return rolePermissions;
    }

    /**
     * 转换批量更新请求为关系列表
     *
     * @param request 批量更新请求
     * @return 关系列表
     */
    public static List<UserRole> convert(BatchUserRoleRequest request) {
        List<UserRole> userRoles = new ArrayList<>();
        if (request.roleIds == null) return userRoles;
        for (String roleId : request.roleIds) {
            UserRole userRole = new UserRole();
            userRole.userId = request.userId;
            userRole.appId = request.appId;
            userRole.roleId = roleId;
            userRoles.add(userRole);
        }
        return userRoles;
    }

    /**
     * 转换批量更新请求为关系列表
     *
     * @param request 批量更新请求
     * @return 关系列表
     */
    public static List<AppUser> convert(BatchAppUserRequest request) {
        List<AppUser> appUsers = new ArrayList<>();
        if (request.userIds == null) return appUsers;
        for (String userId : request.userIds) {
            AppUser appUser = new AppUser();
            appUser.appId = request.appId;
            appUser.userId = userId;
            appUsers.add(appUser);
        }
        return appUsers;
    }
}
