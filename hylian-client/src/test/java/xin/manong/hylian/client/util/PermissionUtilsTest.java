package xin.manong.hylian.client.util;

import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.BadRequestException;

/**
 * @author frankcl
 * @date 2023-10-13 17:39:27
 */
public class PermissionUtilsTest {

    @Test
    public void testValidateSuccess() {
        PermissionUtils.validate("/abc/123/*");
        PermissionUtils.validate("/abc/123/**");
        PermissionUtils.validate("/abc/123/");
    }

    @Test(expected = BadRequestException.class)
    public void testValidateNotStartWithSlash() {
        PermissionUtils.validate("abc/123/");
    }

    @Test(expected = BadRequestException.class)
    public void testValidateNotValidStar() {
        PermissionUtils.validate("/abc/*/123/");
    }

    @Test(expected = BadRequestException.class)
    public void testValidateNotValidStarAnother() {
        PermissionUtils.validate("/abc/*/123/**");
    }

    @Test
    public void testMatch() {
        Assert.assertTrue(PermissionUtils.match("/abc/123/*", "/abc/123/xxx"));
        Assert.assertFalse(PermissionUtils.match("/abc/123/*", "/abc/123/xxx/x"));
        Assert.assertFalse(PermissionUtils.match("/abc/123/*", "/abc/456/xxx"));
        Assert.assertTrue(PermissionUtils.match("/abc/123/**", "/abc/123/xxx/x"));
        Assert.assertTrue(PermissionUtils.match("/abc/123/**", "/abc/123/xxx"));
        Assert.assertFalse(PermissionUtils.match("/abc/123/**", "/abc/456/xxx"));
        Assert.assertTrue(PermissionUtils.match("/abc/123/", "/abc/123/"));
        Assert.assertFalse(PermissionUtils.match("/abc/123/", "/abc/123"));
    }
}
