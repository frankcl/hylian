package xin.manong.hylian.server.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xin.manong.hylian.model.Profile;
import xin.manong.hylian.server.ApplicationTest;
import xin.manong.weapon.base.util.RandomID;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-09-01 17:47:13
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class TokenServiceImplTest {

    @Resource
    protected TokenService tokenService;

    @Test
    public void testBuildAndVerify() {
        Profile profile = new Profile();
        profile.setId(RandomID.build()).setUserId("user").setTenantId("tenant");
        String token = tokenService.buildToken(profile, 600000L);
        Assert.assertFalse(StringUtils.isEmpty(token));
        Assert.assertTrue(tokenService.verifyToken(token));
    }

    @Test
    public void testTokenCache() {
        Profile profile = new Profile();
        profile.setId(RandomID.build()).setUserId("user").setTenantId("tenant");
        String token = tokenService.buildToken(profile, 600000L);
        Assert.assertFalse(StringUtils.isEmpty(token));
        String ticket = "ticket";
        tokenService.putToken(token, ticket);
        Assert.assertEquals(ticket, tokenService.getTicket(token));
        tokenService.removeToken(token);
        Assert.assertNull(tokenService.getTicket(token));
    }
}
