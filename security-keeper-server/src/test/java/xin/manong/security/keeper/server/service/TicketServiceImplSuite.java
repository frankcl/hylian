package xin.manong.security.keeper.server.service;

import org.apache.commons.codec.digest.DigestUtils;
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
import java.util.Set;

/**
 * @author frankcl
 * @date 2023-09-13 10:34:18
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class TicketServiceImplSuite {

    @Resource
    protected TicketService ticketService;

    @Test
    public void testBuildAndVerify() {
        Profile profile = new Profile();
        profile.setId(RandomID.build()).setUserId("user").setTenantId("tenant").setVendorId("vendor");
        String ticket = ticketService.buildTicket(profile, 600000L);
        Assert.assertFalse(StringUtils.isEmpty(ticket));
        Assert.assertTrue(ticketService.verifyTicket(ticket));
    }

    @Test
    public void testTicketCache() {
        Profile profile = new Profile();
        profile.setId(RandomID.build()).setUserId("user").setTenantId("tenant").setVendorId("vendor");
        String ticket = ticketService.buildTicket(profile, 600000L);
        Assert.assertFalse(StringUtils.isEmpty(ticket));
        ticketService.putTicket(profile.id, ticket);
        Assert.assertEquals(ticket, ticketService.getTicket(profile.id));
        ticketService.removeTicket(profile.id);
        Assert.assertTrue(ticketService.getTicket(profile.id) == null);
        ticketService.addToken(profile.id, "token1");
        ticketService.addToken(profile.id, "token2");
        Set<String> tokens = ticketService.getTicketTokens(profile.id);
        Assert.assertTrue(tokens != null && tokens.size() == 2);
        Assert.assertTrue(tokens.contains(DigestUtils.md5Hex("token1")));
        Assert.assertTrue(tokens.contains(DigestUtils.md5Hex("token2")));
        ticketService.removeToken(profile.id, "token1");
        tokens = ticketService.getTicketTokens(profile.id);
        Assert.assertTrue(tokens != null && tokens.size() == 1);
        Assert.assertTrue(tokens.contains(DigestUtils.md5Hex("token2")));
        ticketService.removeTicketTokens(profile.id);
        Assert.assertTrue(ticketService.getTicketTokens(profile.id).isEmpty());
    }
}
