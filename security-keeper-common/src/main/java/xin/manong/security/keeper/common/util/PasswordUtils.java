package xin.manong.security.keeper.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;

/**
 * 密码工具
 *
 * @author frankcl
 * @date 2023-09-12 10:25:31
 */
public class PasswordUtils {

    private static final Logger logger = LoggerFactory.getLogger(PasswordUtils.class);

    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * 检查密码，无效密码抛出异常BadRequestException
     * 1. 最小长度8
     * 2. 安全性：至少包含一个大写字母，小写字母，数字及特殊字符
     *
     * @param password 密码
     */
    public static void checkPassword(String password) {
        password = password == null ? "" : password.trim();
        if (StringUtils.isEmpty(password) || password.length() < MIN_PASSWORD_LENGTH) {
            logger.error("password length[{}] is invalid", password == null ? 0 : password.length());
            throw new BadRequestException(String.format("秘钥最小长度[%d]", password.length()));
        }
        int lowerCaseLetters = 0, upperCaseLetters = 0, digits = 0, others = 0;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (c >= 'A' && c <= 'Z') upperCaseLetters++;
            else if (c >= 'a' && c <= 'z') lowerCaseLetters++;
            else if (c >= '0' && c <= '9') digits++;
            else others++;
        }
        if (lowerCaseLetters == 0 || upperCaseLetters == 0 || digits == 0 || others == 0) {
            logger.error("password security is weak");
            throw new BadRequestException("密码安全性弱，建议包含大小写字母、数字及特殊符号");
        }
    }
}
