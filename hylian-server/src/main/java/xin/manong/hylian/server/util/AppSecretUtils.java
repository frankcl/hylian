package xin.manong.hylian.server.util;

import java.util.Random;

/**
 * 应用秘钥工具
 *
 * @author frankcl
 * @date 2023-09-05 13:36:13
 */
public class AppSecretUtils {

    private static final int DEFAULT_SECRET_CHAR_NUM = 24;

    /* 编码字符集合 */
    private final static char[] CHARS = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z'
    };

    /**
     * 构建24位秘钥
     *
     * @return 秘钥
     */
    public static String buildSecret() {
        return buildSecret(DEFAULT_SECRET_CHAR_NUM);
    }

    /**
     * 构建秘钥
     *
     * @param n 秘钥位数
     * @return 秘钥
     */
    public static String buildSecret(int n) {
        Random random = new Random();
        if (n <= 0) n = DEFAULT_SECRET_CHAR_NUM;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < n; i++) builder.append(CHARS[random.nextInt(CHARS.length)]);
        return builder.toString();
    }
}
