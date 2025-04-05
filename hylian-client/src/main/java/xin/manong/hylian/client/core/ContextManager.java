package xin.manong.hylian.client.core;

import xin.manong.hylian.model.User;
import xin.manong.hylian.client.common.Constants;
import xin.manong.weapon.base.common.Context;

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
        return getValue(Constants.CURRENT_USER, User.class);
    }

    /**
     * 设置用户信息
     *
     * @param user 用户信息
     */
    public static void setUser(User user) {
        setValue(Constants.CURRENT_USER, user);
    }

    /**
     * 移除用户信息
     */
    public static void removeUser() {
        removeValue(Constants.CURRENT_USER);
    }

    /**
     * 设置信息到上下文
     *
     * @param key 键
     * @param value 值
     */
    public static void setValue(String key, Object value) {
        if (value == null) return;
        Context context = THREAD_LOCAL_CONTEXT.get();
        if (context == null) {
            context = new Context();
            THREAD_LOCAL_CONTEXT.set(context);
        }
        context.put(key, value);
    }

    /**
     * 从上下文获取值
     *
     * @param key 键
     * @param valueType 值类型
     * @return 成功返回值，否则返回null
     * @param <T> 值类型
     */
    public static <T> T getValue(String key, Class<T> valueType) {
        Context context = THREAD_LOCAL_CONTEXT.get();
        if (context == null) return null;
        Object value = context.get(key);
        return value == null ? null : valueType.cast(value);
    }

    /**
     * 移除值
     *
     * @param key 键
     */
    public static void removeValue(String key) {
        Context context = THREAD_LOCAL_CONTEXT.get();
        if (context == null) return;
        context.remove(key);
    }

    /**
     * 清除线程上下文
     */
    public static void sweepContext() {
        if (THREAD_LOCAL_CONTEXT.get() == null) return;
        THREAD_LOCAL_CONTEXT.remove();
    }
}
