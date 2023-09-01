package xin.manong.security.keeper.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xin.manong.security.keeper.ApplicationTest;
import xin.manong.security.keeper.model.Profile;
import xin.manong.security.keeper.service.impl.JWTServiceImpl;

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
        profile.userId = "aaa";
        profile.tenantId = "bbb";
        profile.vendorId = "ccc";
        String token = jwtService.buildTicket(profile, JWTServiceImpl.ALGORITHM_HS256, 86400000L);
        Assert.assertFalse(StringUtils.isEmpty(token));
        Assert.assertTrue(jwtService.verifyTicket(token));
        Assert.assertFalse(jwtService.verifyToken(token));
        Profile decodedProfile = jwtService.decodeProfile(token);
        Assert.assertTrue(decodedProfile != null);
        Assert.assertEquals(profile.userId, decodedProfile.userId);
        Assert.assertEquals(profile.tenantId, decodedProfile.tenantId);
        Assert.assertEquals(profile.vendorId, decodedProfile.vendorId);
    }

    @Test
    public void testToken() {
        Profile profile = new Profile();
        profile.userId = "aaa";
        profile.tenantId = "bbb";
        profile.vendorId = "ccc";
        String token = jwtService.buildToken(profile, JWTServiceImpl.ALGORITHM_HS256, 60000L);
        Assert.assertFalse(StringUtils.isEmpty(token));
        Assert.assertTrue(jwtService.verifyToken(token));
        Assert.assertFalse(jwtService.verifyTicket(token));
        Profile decodedProfile = jwtService.decodeProfile(token);
        Assert.assertTrue(decodedProfile != null);
        Assert.assertEquals(profile.userId, decodedProfile.userId);
        Assert.assertEquals(profile.tenantId, decodedProfile.tenantId);
        Assert.assertEquals(profile.vendorId, decodedProfile.vendorId);
    }
}
