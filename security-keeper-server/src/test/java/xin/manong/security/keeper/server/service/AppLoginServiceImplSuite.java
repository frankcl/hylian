package xin.manong.security.keeper.server.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.server.ApplicationTest;
import xin.manong.security.keeper.model.AppLogin;
import xin.manong.security.keeper.server.service.request.AppLoginSearchRequest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author frankcl
 * @date 2023-09-04 11:18:50
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class AppLoginServiceImplSuite {

    @Resource
    protected AppLoginService appLoginService;

    @Test
    @Transactional
    @Rollback
    public void testAppLoginOperations() {
        {
            AppLogin appLogin = new AppLogin();
            appLogin.setTicketId("xxx").setUserId("user1").setSessionId("session1").
                    setAppId("app1").setLogoutURL("http://www.manong.xin/");
            Assert.assertTrue(appLoginService.add(appLogin));
        }
        {
            AppLogin appLogin = new AppLogin();
            appLogin.setTicketId("xxx").setUserId("user1").setSessionId("session2").
                    setAppId("app2").setLogoutURL("http://www.sina.com.cn/");
            Assert.assertTrue(appLoginService.add(appLogin));
        }
        {
            AppLoginSearchRequest searchRequest = new AppLoginSearchRequest();
            searchRequest.appId = "app1";
            searchRequest.sessionId = "session1";
            searchRequest.userId = "user1";
            Assert.assertTrue(appLoginService.isLoginApp(searchRequest));
        }
        {
            AppLoginSearchRequest searchRequest = new AppLoginSearchRequest();
            searchRequest.appId = "app2";
            searchRequest.sessionId = "session2";
            searchRequest.userId = "user1";
            Assert.assertTrue(appLoginService.isLoginApp(searchRequest));
        }
        {
            AppLoginSearchRequest searchRequest = new AppLoginSearchRequest();
            searchRequest.appId = "app3";
            searchRequest.sessionId = "session3";
            searchRequest.userId = "user1";
            Assert.assertFalse(appLoginService.isLoginApp(searchRequest));
        }
        {
            List<AppLogin> appLogins = appLoginService.getAppLogins("xxx");
            Assert.assertTrue(appLogins != null && appLogins.size() == 2);
            Assert.assertEquals("xxx", appLogins.get(0).ticketId);
            Assert.assertEquals("user1", appLogins.get(0).userId);
            Assert.assertEquals("app1", appLogins.get(0).appId);
            Assert.assertEquals("session1", appLogins.get(0).sessionId);
            Assert.assertEquals("http://www.manong.xin/", appLogins.get(0).logoutURL);
            Assert.assertTrue(appLogins.get(0).createTime > 0);
            Assert.assertTrue(appLogins.get(0).updateTime > 0);

            Assert.assertEquals("xxx", appLogins.get(1).ticketId);
            Assert.assertEquals("user1", appLogins.get(1).userId);
            Assert.assertEquals("app2", appLogins.get(1).appId);
            Assert.assertEquals("session2", appLogins.get(1).sessionId);
            Assert.assertEquals("http://www.sina.com.cn/", appLogins.get(1).logoutURL);
            Assert.assertTrue(appLogins.get(1).createTime > 0);
            Assert.assertTrue(appLogins.get(1).updateTime > 0);
        }
        {
            AppLoginSearchRequest searchRequest = new AppLoginSearchRequest();
            searchRequest.appId = "app1";
            searchRequest.userId = "user1";
            Pager<AppLogin> pager = appLoginService.search(searchRequest);
            Assert.assertTrue(pager != null && pager.total == 1 && pager.records.size() == 1);
        }
        {
            appLoginService.removeAppLogins("xxx");
            List<AppLogin> appLogins = appLoginService.getAppLogins("xxx");
            Assert.assertTrue(appLogins == null || appLogins.isEmpty());
        }
    }
}
