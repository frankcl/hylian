package xin.manong.hylian.server.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.hylian.server.ApplicationTest;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.model.App;
import xin.manong.hylian.server.service.request.AppSearchRequest;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-09-01 14:49:33
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class AppServiceImplTest {

    @Resource
    protected AppService appService;

    @Test(expected = IllegalStateException.class)
    @Transactional
    @Rollback
    public void testAddSameId() {
        {
            App app = new App();
            app.id = "xxx";
            app.name = "_test_";
            app.secret = "abc";
            Assert.assertTrue(appService.add(app));
        }
        {
            App app = new App();
            app.id = "xxx";
            app.name = "test1";
            app.secret = "abc";
            appService.add(app);
        }
    }

    @Test(expected = IllegalStateException.class)
    @Transactional
    @Rollback
    public void testAddSameName() {
        {
            App app = new App();
            app.id = "xxx";
            app.name = "_test_";
            app.secret = "abc";
            Assert.assertTrue(appService.add(app));
        }
        {
            App app = new App();
            app.id = "xxxx";
            app.name = "_test_";
            app.secret = "abc";
            appService.add(app);
        }
    }

    @Test
    @Transactional
    @Rollback
    public void testAppOperations() {
        {
            App app = new App();
            app.id = "xxx";
            app.name = "_test_";
            app.secret = "abc";
            Assert.assertTrue(appService.add(app));
        }
        {
            App app = appService.get("xxx");
            Assert.assertNotNull(app);
            Assert.assertEquals("xxx", app.id);
            Assert.assertEquals("_test_", app.name);
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
            Assert.assertNotNull(app);
            Assert.assertEquals("xxx", app.id);
            Assert.assertEquals("test123", app.name);
            Assert.assertEquals("abcd", app.secret);
            Assert.assertTrue(app.createTime > 0);
            Assert.assertTrue(app.updateTime > 0);
        }
        {
            AppSearchRequest searchRequest = new AppSearchRequest();
            searchRequest.name = "test123";
            Pager<App> pager = appService.search(searchRequest);
            Assert.assertTrue(pager != null && pager.total == 1 && pager.records.size() == 1);
        }
        {
            Assert.assertTrue(appService.delete("xxx"));
        }
    }
}
