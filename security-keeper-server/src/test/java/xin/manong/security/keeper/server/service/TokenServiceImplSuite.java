package xin.manong.security.keeper.server.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xin.manong.security.keeper.model.Profile;
import xin.manong.security.keeper.server.ApplicationTest;
import xin.manong.weapon.base.util.RandomID;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-09-01 17:47:13
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class TokenServiceImplSuite {

    @Resource
    protected TokenService tokenService;

    @Test
    public void testBuildAndVerify() {
        Profile profile = new Profile();
        profile.setId(RandomID.build()).setUserId("user").setTenantId("tenant").setVendorId("vendor");
        String token = tokenService.buildToken(profile, 600000L);
        Assert.assertFalse(StringUtils.isEmpty(token));
        Assert.assertTrue(tokenService.verifyToken(token));
    }

    @Test
    public void testTokenCache() {
        Profile profile = new Profile();
        profile.setId(RandomID.build()).setUserId("user").setTenantId("tenant").setVendorId("vendor");
        String token = tokenService.buildToken(profile, 600000L);
        Assert.assertFalse(StringUtils.isEmpty(token));
        String ticket = "ticket";
        tokenService.putTokenTicket(token, ticket);
        Assert.assertEquals(ticket, tokenService.getTicket(token));
        tokenService.removeTokenTicket(token);
        Assert.assertNull(tokenService.getTicket(token));
    }
}
