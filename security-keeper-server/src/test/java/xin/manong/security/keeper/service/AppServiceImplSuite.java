package xin.manong.security.keeper.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.security.keeper.ApplicationTest;
import xin.manong.security.keeper.dao.model.Pager;
import xin.manong.security.keeper.model.App;
import xin.manong.security.keeper.service.request.AppSearchRequest;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-09-01 14:49:33
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class AppServiceImplSuite {

    @Resource
    protected AppService appService;

    @Test
    @Transactional
    @Rollback
    public void testAppOperations() {
        {
            App app = new App();
            app.id = "xxx";
            app.name = "test";
            app.secret = "abc";
            Assert.assertTrue(appService.add(app));
        }
        {
            App app = new App();
            app.id = "xxx";
            app.name = "test1";
            app.secret = "abc";
            try {
                Assert.assertFalse(appService.add(app));
                Assert.assertTrue(false);
            } catch (Exception e) {
            }
        }
        {
            App app = new App();
            app.id = "xxxx";
            app.name = "test";
            app.secret = "abc";
            try {
                Assert.assertFalse(appService.add(app));
                Assert.assertTrue(false);
            } catch (Exception e) {
            }
        }
        {
            App app = appService.get("xxx");
            Assert.assertTrue(app != null);
            Assert.assertEquals("xxx", app.id);
            Assert.assertEquals("test", app.name);
            Assert.assertEquals("abc", app.secret);
            Assert.assertTrue(app.createTime > 0);
            Assert.assertTrue(app.updateTime > 0);
        }
        {
            App app = new App();
            app.id = "xxx";
            app.name = "test123";
            app.secret = "abcd";
            Assert.assertTrue(appService.update(app));
        }
        {
            App app = appService.get("xxx");
            Assert.assertTrue(app != null);
            Assert.assertEquals("xxx", app.id);
            Assert.assertEquals("test123", app.name);
            Assert.assertEquals("abcd", app.secret);
            Assert.assertTrue(app.createTime > 0);
            Assert.assertTrue(app.updateTime > 0);
        }
        {
            AppSearchRequest searchRequest = new AppSearchRequest();
            searchRequest.name = "test";
            Pager<App> pager = appService.search(searchRequest);
            Assert.assertTrue(pager != null && pager.total == 1 && pager.records.size() == 1);
        }
        {
            Assert.assertTrue(appService.delete("xxx"));
        }
    }
}
