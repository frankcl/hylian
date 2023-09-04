package xin.manong.security.keeper.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xin.manong.security.keeper.ApplicationTest;
import xin.manong.security.keeper.common.Constants;
import xin.manong.security.keeper.model.Profile;
import xin.manong.weapon.base.util.RandomID;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-08-31 20:00:24
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class JWTServiceImplSuite {

    @Resource
    protected JWTService jwtService;

    @Test
    public void testTicket() {
        Profile profile = new Profile();
        profile.id = RandomID.build();
        profile.userId = "aaa";
        profile.tenantId = "bbb";
        profile.vendorId = "ccc";
        String token = jwtService.buildTicket(profile, Constants.ALGORITHM_HS256, 86400000L);
        Assert.assertFalse(StringUtils.isEmpty(token));
        Assert.assertTrue(jwtService.verifyTicket(token));
        Assert.assertFalse(jwtService.verifyToken(token));
        Profile decodedProfile = jwtService.decodeProfile(token);
        Assert.assertTrue(decodedProfile != null);
        Assert.assertEquals(profile.id, decodedProfile.id);
        Assert.assertEquals(profile.userId, decodedProfile.userId);
        Assert.assertEquals(profile.tenantId, decodedProfile.tenantId);
        Assert.assertEquals(profile.vendorId, decodedProfile.vendorId);
    }

    @Test
    public void testToken() {
        Profile profile = new Profile();
        profile.id = RandomID.build();
        profile.userId = "aaa";
        profile.tenantId = "bbb";
        profile.vendorId = "ccc";
        String token = jwtService.buildToken(profile, Constants.ALGORITHM_HS256, 60000L);
        Assert.assertFalse(StringUtils.isEmpty(token));
        Assert.assertTrue(jwtService.verifyToken(token));
        Assert.assertFalse(jwtService.verifyTicket(token));
        Profile decodedProfile = jwtService.decodeProfile(token);
        Assert.assertTrue(decodedProfile != null);
        Assert.assertEquals(profile.userId, decodedProfile.userId);
        Assert.assertEquals(profile.tenantId, decodedProfile.tenantId);
        Assert.assertEquals(profile.vendorId, decodedProfile.vendorId);
        Assert.assertEquals(profile.id, decodedProfile.id);
    }

    @Test
    public void testBuildTokenWithTicket() {
        Profile profile = new Profile();
        profile.id = RandomID.build();
        profile.userId = "aaa";
        profile.tenantId = "bbb";
        profile.vendorId = "ccc";
        String ticket = jwtService.buildTicket(profile, Constants.ALGORITHM_HS256, 60000L);
        Assert.assertTrue(!StringUtils.isEmpty(ticket));
        String token = jwtService.buildTokenWithTicket(ticket, 60000L);
        Assert.assertTrue(!StringUtils.isEmpty(token));
        Assert.assertTrue(jwtService.verifyToken(token));
        Assert.assertEquals(profile.id, jwtService.decodeProfile(token).id);
    }
}
