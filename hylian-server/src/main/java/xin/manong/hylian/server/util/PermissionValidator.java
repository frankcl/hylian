package xin.manong.hylian.server.util;

import xin.manong.hylian.client.core.ContextManager;
import xin.manong.hylian.model.User;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import java.util.List;

/**
 * 权限验证器
 *
 * @author frankcl
 * @date 2024-10-15 18:26:29
 */
public class PermissionValidator {

    /**
     * 验证是否具备操作应用权限
     * 如果不具备权限抛出异常
     *
     * @param appId 应用ID
     */
    public static void validateAppPermission(String appId) {
        User currentUser = ContextManager.getUser();
        if (currentUser == null) throw new NotAuthorizedException("用户尚未登录");
        if (currentUser.superAdmin) return;
        List<String> appIds = ContextManager.getFollowApps();
        if (appIds == null || !appIds.contains(appId)) throw new ForbiddenException("无权操作数据");
    }
}
