package xin.manong.hylian.server.converter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.hylian.model.*;
import xin.manong.hylian.server.request.*;
import xin.manong.hylian.model.view.response.ViewTenant;
import xin.manong.hylian.model.view.response.ViewUser;

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
     * 转换用户请求为用户对象
     *
     * @param userRequest 用户请求
     * @return 用户对象
     */
    public static User convert(UserRequest userRequest) {
        if (userRequest == null) return null;
        User user = new User();
        user.userName = userRequest.userName;
        user.password = userRequest.password;
        user.name = userRequest.name;
        user.tenantId = userRequest.tenantId;
        user.avatar = userRequest.avatar;
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
        tenant.vendorId = tenantRequest.vendorId;
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
        tenant.vendorId = tenantRequest.vendorId;
        return tenant;
    }

    /**
     * 转换供应商请求为供应商对象
     *
     * @param vendorRequest 供应商请求
     * @return 供应商对象
     */
    public static Vendor convert(VendorRequest vendorRequest) {
        if (vendorRequest == null) return null;
        Vendor vendor = new Vendor();
        vendor.name = vendorRequest.name;
        return vendor;
    }

    /**
     * 转换供应商更新请求为供应商对象
     *
     * @param vendorRequest 供应商更新请求
     * @return 供应商对象
     */
    public static Vendor convert(VendorUpdateRequest vendorRequest) {
        if (vendorRequest == null) return null;
        Vendor vendor = new Vendor();
        vendor.id = vendorRequest.id;
        vendor.name = vendorRequest.name;
        return vendor;
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
        permission.resource = permissionRequest.resource;
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
        permission.resource = permissionRequest.resource;
        permission.appId = permissionRequest.appId;
        return permission;
    }

    /**
     * 转换租户信息为视图层租户信息
     *
     * @param tenant 租户信息
     * @param vendor 供应商信息
     * @return 视图层租户信息
     */
    public static ViewTenant convert(Tenant tenant, Vendor vendor) {
        if (tenant == null) return null;
        ViewTenant viewTenant = new ViewTenant();
        viewTenant.id = tenant.id;
        viewTenant.name = tenant.name;
        viewTenant.createTime = tenant.createTime;
        viewTenant.updateTime = tenant.updateTime;
        viewTenant.vendor = vendor;
        if (vendor != null && !vendor.id.equals(tenant.vendorId)) {
            logger.error("vendors are not consistent for tenant[{}] and vendor[{}]", tenant.vendorId, vendor.id);
            throw new IllegalStateException("供应商ID不一致");
        }
        return viewTenant;
    }

    /**
     * 转换用户信息为视图层用户信息
     *
     * @param user 用户信息
     * @param tenant 视图层租户信息
     * @return 视图层用户信息
     */
    public static ViewUser convert(User user, ViewTenant tenant) {
        if (user == null) return null;
        ViewUser viewUser = new ViewUser();
        viewUser.id = user.id;
        viewUser.userName = user.userName;
        viewUser.name = user.name;
        viewUser.avatar = user.avatar;
        viewUser.createTime = user.createTime;
        viewUser.updateTime = user.updateTime;
        viewUser.tenant = tenant;
        if (tenant != null) {
            viewUser.vendor = tenant.vendor;
            if (!user.tenantId.equals(tenant.id)) {
                logger.error("tenants are not consistent for user[{}] and tenant[{}]", user.tenantId, tenant.id);
                throw new IllegalStateException("租户ID不一致");
            }
            if (tenant.vendor != null && !user.vendorId.equals(tenant.vendor.id)) {
                logger.error("vendors are not consistent for user[{}] and vendor[{}]", user.vendorId, tenant.vendor.id);
                throw new IllegalStateException("供应商ID不一致");
            }
        }
        return viewUser;
    }
}
