package xin.manong.hylian.client.core;

import xin.manong.hylian.client.util.SessionUtils;
import xin.manong.hylian.model.User;
import xin.manong.hylian.client.common.Constants;
import xin.manong.weapon.base.common.Context;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 线程上下文管理器
 *
 * @author frankcl
 * @date 2023-09-27 14:53:07
 */
public class ContextManager {

    private final static ThreadLocal<Context> THREAD_LOCAL_CONTEXT = new ThreadLocal<>();

    /**
     * 获取用户信息
     *
     * @return 成功返回用户信息，否则返回null
     */
    public static User getUser() {
        Context context = THREAD_LOCAL_CONTEXT.get();
        if (context == null) return null;
        return (User) context.get(Constants.CURRENT_USER);
    }

    /**
     * 获取关注应用列表
     *
     * @return 成功返回关注应用列表，否则返回空列表
     */
    @SuppressWarnings("unchecked")
    public static List<String> getFollowApps() {
        Context context = THREAD_LOCAL_CONTEXT.get();
        if (context == null) return new ArrayList<>();
        return (List<String>) context.get(Constants.CURRENT_FOLLOW_APPS);
    }

    /**
     * 设置关注应用列表
     *
     * @param followApps 关注应用列表
     */
    public static void setFollowApps(List<String> followApps) {
        Context context = THREAD_LOCAL_CONTEXT.get();
        if (context == null) {
            context = new Context();
            THREAD_LOCAL_CONTEXT.set(context);
        }
        context.put(Constants.CURRENT_FOLLOW_APPS, followApps);
    }

    /**
     * 移除关注应用列表
     */
    public static void removeFollowApps() {
        Context context = THREAD_LOCAL_CONTEXT.get();
        if (context == null) return;
        context.remove(Constants.CURRENT_FOLLOW_APPS);
    }

    /**
     * 填充线程上下文
     * 1. 填充用户信息
     * 2. 填充租户信息
     *
     * @param httpRequest HTTP请求
     */
    public static void fillContext(HttpServletRequest httpRequest) {
        Context context = THREAD_LOCAL_CONTEXT.get();
        if (context == null) {
            context = new Context();
            THREAD_LOCAL_CONTEXT.set(context);
        }
        context.put(Constants.CURRENT_USER, SessionUtils.getUser(httpRequest));
    }

    /**
     * 清除线程上下文
     */
    public static void sweepContext() {
        if (THREAD_LOCAL_CONTEXT.get() == null) return;
        THREAD_LOCAL_CONTEXT.remove();
    }
}
