package xin.manong.security.keeper.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.security.keeper.ApplicationTest;
import xin.manong.security.keeper.model.LoginApp;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author frankcl
 * @date 2023-09-04 11:18:50
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class LoginAppServiceImplSuite {

    @Resource
    protected LoginAppService loginAppService;

    @Test
    @Transactional
    @Rollback
    public void testLoginAppOperations() {
        {
            LoginApp loginApp = new LoginApp(
                    "xxx", "user1", "app1", "http://www.manong.xin/");
            Assert.assertTrue(loginAppService.register(loginApp));
        }
        {
            LoginApp loginApp = new LoginApp(
                    "xxx", "user1", "app2", "http://www.sina.com.cn/");
            Assert.assertTrue(loginAppService.register(loginApp));
        }
        {
            Assert.assertTrue(loginAppService.isLoginApp("xxx", "app1"));
            Assert.assertTrue(loginAppService.isLoginApp("xxx", "app2"));
            Assert.assertFalse(loginAppService.isLoginApp("xxx", "app3"));
        }
        {
            List<LoginApp> loginApps = loginAppService.getLoginApps("xxx");
            Assert.assertTrue(loginApps != null && loginApps.size() == 2);
            Assert.assertEquals("xxx", loginApps.get(0).ticketId);
            Assert.assertEquals("user1", loginApps.get(0).userId);
            Assert.assertEquals("app1", loginApps.get(0).appId);
            Assert.assertEquals("http://www.manong.xin/", loginApps.get(0).baseURL);
            Assert.assertTrue(loginApps.get(0).createTime > 0);
            Assert.assertTrue(loginApps.get(0).updateTime > 0);

            Assert.assertEquals("xxx", loginApps.get(1).ticketId);
            Assert.assertEquals("user1", loginApps.get(1).userId);
            Assert.assertEquals("app2", loginApps.get(1).appId);
            Assert.assertEquals("http://www.sina.com.cn/", loginApps.get(1).baseURL);
            Assert.assertTrue(loginApps.get(1).createTime > 0);
            Assert.assertTrue(loginApps.get(1).updateTime > 0);
        }
        {
            loginAppService.removeLoginApps("xxx");
            List<LoginApp> loginApps = loginAppService.getLoginApps("xxx");
            Assert.assertTrue(loginApps == null || loginApps.isEmpty());
        }
    }
}
