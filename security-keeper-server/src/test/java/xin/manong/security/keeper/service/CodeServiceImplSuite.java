package xin.manong.security.keeper.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xin.manong.security.keeper.ApplicationTest;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-09-01 17:47:13
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class CodeServiceImplSuite {

    @Resource
    protected CodeService codeService;

    @Test
    public void testCodeOperations() {
        String ticket = "ticket";
        String code = codeService.createCode(ticket);
        Assert.assertFalse(StringUtils.isBlank(code));
        Assert.assertEquals("ticket", codeService.getTicket(code));
        Assert.assertTrue(codeService.removeCode(code));
    }
}
