package xin.manong.hylian.client.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 权限工具
 *
 * @author frankcl
 * @date 2023-10-13 17:24:23
 */
public class PermissionUtils {

    private static final Logger logger = LoggerFactory.getLogger(PermissionUtils.class);

    /**
     * 校验模式合法性
     *
     * @param pattern 模式
     * @return 合法返回true，否则返回false
     */
    public static boolean validatePattern(String pattern) {
        if (StringUtils.isEmpty(pattern)) {
            logger.error("pattern is empty");
            return false;
        }
        if (!pattern.startsWith("/")) {
            logger.error("pattern[{}] must starts with /", pattern);
            return false;
        }
        if (pattern.endsWith("/*")) pattern = pattern.substring(0, pattern.length() - 2);
        if (pattern.endsWith("/**")) pattern = pattern.substring(0, pattern.length() - 3);
        if (pattern.contains("*")) {
            logger.error("invalid pattern[{}]", pattern);
            return false;
        }
        return true;
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
