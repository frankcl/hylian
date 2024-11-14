package xin.manong.hylian.server.service;

import jakarta.annotation.Resource;
import jakarta.ws.rs.NotFoundException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.hylian.server.ApplicationTest;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.model.Tenant;
import xin.manong.hylian.model.User;
import xin.manong.hylian.server.service.request.UserSearchRequest;

/**
 * @author frankcl
 * @date 2023-09-01 15:58:32
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class UserServiceImplTest {

    @Resource
    protected UserService userService;
    @Resource
    protected TenantService tenantService;

    @Before
    public void setUp() {
        Tenant tenant = new Tenant();
        tenant.id = "t_abc";
        tenant.name = "test_tenant";
        Assert.assertTrue(tenantService.add(tenant));
    }

    @After
    public void tearDown() {
        Assert.assertTrue(tenantService.delete("t_abc"));
    }

    @Test(expected = IllegalStateException.class)
    @Transactional
    @Rollback
    public void testAddSameId() {
        {
            User user = new User();
            user.id = "xxx";
            user.username = "abc";
            user.name = "frankcl";
            user.tenantId = "t_abc";
            user.password = "xxxxxx";
            Assert.assertTrue(userService.add(user));
        }
        try {
            User user = new User();
            user.id = "xxx";
            user.username = "abcd";
            user.name = "frankcl";
            user.tenantId = "t_abc";
            user.password = "xxxxxx";
            userService.add(user);
        } finally {
            Assert.assertTrue(userService.delete("xxx"));
        }
    }

    @Test(expected = IllegalStateException.class)
    @Transactional
    @Rollback
    public void testAddSameUsername() {
        {
            User user = new User();
            user.id = "xxx";
            user.username = "abc";
            user.name = "frankcl";
            user.tenantId = "t_abc";
            user.password = "xxxxxx";
            Assert.assertTrue(userService.add(user));
        }
        try {
            User user = new User();
            user.id = "xxxx";
            user.username = "abc";
            user.name = "frank";
            user.tenantId = "t_abc";
            user.password = "xxxxxx";
            userService.add(user);
        } finally {
            Assert.assertTrue(userService.delete("xxx"));
        }
    }

    @Test(expected = NotFoundException.class)
    @Transactional
    @Rollback
    public void testAddNotFoundTenant() {
        User user = new User();
        user.id = "xxx";
        user.username = "abc";
        user.name = "frankcl";
        user.tenantId = "t_abcd";
        user.password = "xxxxxx";
        Assert.assertTrue(userService.add(user));
    }

    @Test
    @Transactional
    @Rollback
    public void testUserOperations() {
        {
            User user = new User();
            user.id = "xxx";
            user.username = "abc";
            user.name = "frankcl";
            user.tenantId = "t_abc";
            user.password = "xxxxxx";
            Assert.assertTrue(userService.add(user));
        }
        {
            User user = userService.get("xxx");
            Assert.assertNotNull(user);
            Assert.assertEquals("xxx", user.id);
            Assert.assertEquals("abc", user.username);
            Assert.assertEquals("frankcl", user.name);
            Assert.assertEquals("t_abc", user.tenantId);
            Assert.assertEquals("dad3a37aa9d50688b5157698acfd7aee", user.password);
            Assert.assertTrue(user.createTime > 0);
            Assert.assertTrue(user.updateTime > 0);
        }
        {
            User user = new User();
            user.id = "xxx";
            user.password = "xxxxxxx";
            Assert.assertTrue(userService.update(user));
        }
        {
            User user = userService.get("xxx");
            Assert.assertNotNull(user);
            Assert.assertEquals("xxx", user.id);
            Assert.assertEquals("abc", user.username);
            Assert.assertEquals("frankcl", user.name);
            Assert.assertEquals("t_abc", user.tenantId);
            Assert.assertEquals("04adb4e2f055c978c9bb101ee1bc5cd4", user.password);
            Assert.assertTrue(user.createTime > 0);
            Assert.assertTrue(user.updateTime > 0);
        }
        {
            UserSearchRequest searchRequest = new UserSearchRequest();
            searchRequest.tenantId = "t_abc";
            searchRequest.username = "abc";
            searchRequest.name = "frank";
            Pager<User> pager = userService.search(searchRequest);
            Assert.assertTrue(pager != null && pager.total == 1 && pager.records.size() == 1);
        }
        {
            Assert.assertTrue(userService.delete("xxx"));
        }
    }
}
