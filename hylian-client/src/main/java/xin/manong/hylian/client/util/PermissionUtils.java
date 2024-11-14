package xin.manong.hylian.client.util;

import jakarta.ws.rs.BadRequestException;
import org.apache.commons.lang3.StringUtils;

/**
 * 权限工具
 *
 * @author frankcl
 * @date 2023-10-13 17:24:23
 */
public class PermissionUtils {

    /**
     * 校验资源路径合法性
     *
     * @param pattern 资源路径
     */
    public static void validate(String pattern) {
        if (StringUtils.isEmpty(pattern)) throw new BadRequestException("资源路径为空");
        if (!pattern.startsWith("/")) throw new BadRequestException("资源路径必须以 / 开始");
        if (pattern.endsWith("/*")) pattern = pattern.substring(0, pattern.length() - 2);
        if (pattern.endsWith("/**")) pattern = pattern.substring(0, pattern.length() - 3);
        if (pattern.contains("*")) throw new BadRequestException("非法资源路径，只能以 /* 或 /** 结尾");
    }

    /**
     * 判断访问路径是否符合模式
     *
     * @param pattern 模式
     * @param path 访问路径
     * @return 符合返回true，否则返回false
     */
    public static boolean match(String pattern, String path) {
        if (StringUtils.isEmpty(pattern) || StringUtils.isEmpty(path)) return false;
        if (!path.startsWith("/")) path = String.format("/%s", path);
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            return path.startsWith(prefix);
        } else if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            if (!path.startsWith(prefix)) return false;
            String rest = path.substring(prefix.length());
            return !rest.contains("/");
        } else {
            return pattern.equals(path);
        }
    }
}
