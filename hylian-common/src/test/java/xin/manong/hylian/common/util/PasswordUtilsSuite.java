package xin.manong.hylian.common.util;

import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.BadRequestException;

/**
 * @author frankcl
 * @date 2023-09-12 10:29:58
 */
public class PasswordUtilsSuite {

    @Test
    public void checkPasswordSuccess() {
        PasswordUtils.checkPassword("xmdferfA1dfd4!");
    }

    @Test
    public void checkPasswordFail() {
        try {
            PasswordUtils.checkPassword("aA1#");
            Assert.assertTrue(false);
        } catch (BadRequestException e) {
        }
        try {
            PasswordUtils.checkPassword("aA112123aaa");
            Assert.assertTrue(false);
        } catch (BadRequestException e) {
        }
        try {
            PasswordUtils.checkPassword("a@#$112123aaa");
            Assert.assertTrue(false);
        } catch (BadRequestException e) {
        }
        try {
            PasswordUtils.checkPassword("aAffgfgfvc^aaa");
            Assert.assertTrue(false);
        } catch (BadRequestException e) {
        }
    }
}
