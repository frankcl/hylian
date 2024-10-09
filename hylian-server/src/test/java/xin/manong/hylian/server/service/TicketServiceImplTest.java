package xin.manong.hylian.server.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xin.manong.hylian.server.model.UserProfile;
import xin.manong.hylian.server.ApplicationTest;
import xin.manong.weapon.base.util.RandomID;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author frankcl
 * @date 2023-09-13 10:34:18
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class TicketServiceImplTest {

    @Resource
    protected TicketService ticketService;

    @Test
    public void testBuildAndVerify() {
        UserProfile userProfile = new UserProfile();
        userProfile.setId(RandomID.build()).setUserId("user").setTenantId("tenant");
        String ticket = ticketService.buildTicket(userProfile, 600000L);
        Assert.assertFalse(StringUtils.isEmpty(ticket));
        Assert.assertTrue(ticketService.verifyTicket(ticket));
    }

    @Test
    public void testTicketCache() {
        UserProfile userProfile = new UserProfile();
        userProfile.setId(RandomID.build()).setUserId("user").setTenantId("tenant");
        String ticket = ticketService.buildTicket(userProfile, 600000L);
        Assert.assertFalse(StringUtils.isEmpty(ticket));
        ticketService.putTicket(userProfile.id, ticket);
        Assert.assertEquals(ticket, ticketService.getTicket(userProfile.id));
        ticketService.removeTicket(userProfile.id);
        Assert.assertNull(ticketService.getTicket(userProfile.id));
        ticketService.addToken(userProfile.id, "token1");
        ticketService.addToken(userProfile.id, "token2");
        Set<String> tokens = ticketService.getTokens(userProfile.id);
        Assert.assertTrue(tokens != null && tokens.size() == 2);
        Assert.assertTrue(tokens.contains(DigestUtils.md5Hex("token1")));
        Assert.assertTrue(tokens.contains(DigestUtils.md5Hex("token2")));
        ticketService.removeToken(userProfile.id, "token1");
        tokens = ticketService.getTokens(userProfile.id);
        Assert.assertTrue(tokens != null && tokens.size() == 1);
        Assert.assertTrue(tokens.contains(DigestUtils.md5Hex("token2")));
        ticketService.removeTokens(userProfile.id);
        Assert.assertTrue(ticketService.getTokens(userProfile.id).isEmpty());
    }
}
