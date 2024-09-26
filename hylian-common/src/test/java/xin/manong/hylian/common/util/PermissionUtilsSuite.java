package xin.manong.hylian.common.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author frankcl
 * @date 2023-10-13 17:39:27
 */
public class PermissionUtilsSuite {

    @Test
    public void testValidatePattern() {
        Assert.assertTrue(PermissionUtils.validatePattern("/abc/123/*"));
        Assert.assertTrue(PermissionUtils.validatePattern("/abc/123/**"));
        Assert.assertTrue(PermissionUtils.validatePattern("/abc/123/"));
        Assert.assertFalse(PermissionUtils.validatePattern("abc/123/"));
        Assert.assertFalse(PermissionUtils.validatePattern("/abc/*/123/"));
        Assert.assertFalse(PermissionUtils.validatePattern("/abc/*/123/*"));
        Assert.assertFalse(PermissionUtils.validatePattern("/abc/*/123/**"));
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
