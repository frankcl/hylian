package xin.manong.hylian.server.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author frankcl
 * @date 2023-09-05 13:41:54
 */
public class AppSecretUtilsTest {

    @Test
    public void testBuildSecret() {
        {
            String secret = AppSecretUtils.buildSecret();
            Assert.assertEquals(24, secret.length());
        }
        {
            String secret = AppSecretUtils.buildSecret(20);
            Assert.assertEquals(20, secret.length());
        }
        {
            String secret = AppSecretUtils.buildSecret(-1);
            Assert.assertEquals(24, secret.length());
        }
    }
}
