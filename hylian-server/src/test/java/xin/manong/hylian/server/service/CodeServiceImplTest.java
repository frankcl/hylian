package xin.manong.hylian.server.service;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xin.manong.hylian.server.ApplicationTest;

/**
 * @author frankcl
 * @date 2023-09-01 17:47:13
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class CodeServiceImplTest {

    @Resource
    protected CodeService codeService;

    @Test
    public void testCodeOperations() {
        String ticket = "ticket";
        String code = codeService.createCode(ticket);
        Assert.assertFalse(StringUtils.isEmpty(code));
        Assert.assertEquals("ticket", codeService.getTicket(code));
        Assert.assertTrue(codeService.removeCode(code));
    }
}
