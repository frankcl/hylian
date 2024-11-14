package xin.manong.hylian.server.service;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xin.manong.hylian.server.ApplicationTest;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.model.UserProfile;
import xin.manong.weapon.base.util.RandomID;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author frankcl
 * @date 2023-08-31 20:00:24
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class JWTServiceImplTest {

    @Resource
    protected JWTService jwtService;

    @Test
    public void testJWTOperations() {
        UserProfile userProfile = new UserProfile();
        userProfile.id = RandomID.build();
        userProfile.userId = "aaa";
        Date expiresAt = new Date(System.currentTimeMillis() + 86400000L);
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(Constants.JWT_HEADER_CATEGORY, Constants.JWT_CATEGORY_TOKEN);
        String token = jwtService.buildJWT(userProfile, expiresAt, Constants.ALGORITHM_HS256, headerMap);
        Assert.assertFalse(StringUtils.isEmpty(token));
        DecodedJWT decodedJWT = jwtService.decodeJWT(token);
        Assert.assertNotNull(decodedJWT);
        Claim claim = decodedJWT.getHeaderClaim(Constants.JWT_HEADER_CATEGORY);
        Assert.assertNotNull(claim);
        Assert.assertEquals(Constants.JWT_CATEGORY_TOKEN, claim.asString());
        Assert.assertTrue(jwtService.verify(decodedJWT));
        UserProfile decodedUserProfile = jwtService.decodeProfile(token);
        Assert.assertNotNull(decodedUserProfile);
        Assert.assertEquals(userProfile.id, decodedUserProfile.id);
        Assert.assertEquals(userProfile.userId, decodedUserProfile.userId);
    }
}
