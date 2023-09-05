package xin.manong.security.keeper.server.converter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.Tenant;
import xin.manong.security.keeper.model.User;
import xin.manong.security.keeper.model.Vendor;
import xin.manong.security.keeper.server.response.ViewTenant;
import xin.manong.security.keeper.server.response.ViewUser;

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
            logger.error("vendor ids are not consistent for {} and {}", tenant.vendorId, vendor.id);
            throw new RuntimeException("供应商ID不一致");
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
        viewUser.roles = user.roles;
        viewUser.createTime = user.createTime;
        viewUser.updateTime = user.updateTime;
        viewUser.tenant = tenant;
        if (tenant != null) {
            viewUser.vendor = tenant.vendor;
            if (user.tenantId.equals(tenant.id)) {
                logger.error("tenant ids are not consistent for {} and {}", user.tenantId, tenant.id);
                throw new RuntimeException("租户ID不一致");
            }
            if (tenant.vendor != null && user.vendorId.equals(tenant.vendor.id)) {
                logger.error("vendor ids are not consistent for {} and {}", user.vendorId, tenant.vendor.id);
                throw new RuntimeException("供应商ID不一致");
            }
        }
        return viewUser;
    }
}
