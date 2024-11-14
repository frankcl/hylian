package xin.manong.hylian.server.util;

import jakarta.ws.rs.BadRequestException;
import org.junit.Test;

/**
 * @author frankcl
 * @date 2023-09-12 10:29:58
 */
public class PasswordUtilsTest {

    @Test
    public void checkPass() {
        PasswordUtils.checkPassword("xmdferfA1dfd4!");
    }

    @Test(expected = BadRequestException.class)
    public void checkLengthNotEnough() {
        PasswordUtils.checkPassword("aA1#");
    }

    @Test(expected = BadRequestException.class)
    public void checkMissSpecial() {
        PasswordUtils.checkPassword("aA112123aaa");
    }

    @Test(expected = BadRequestException.class)
    public void checkMissUpperCase() {
        PasswordUtils.checkPassword("a@#$112123aaa");
    }

    @Test(expected = BadRequestException.class)
    public void checkMissDigits() {
        PasswordUtils.checkPassword("aAffgfgfvc^aaa");
    }
}
