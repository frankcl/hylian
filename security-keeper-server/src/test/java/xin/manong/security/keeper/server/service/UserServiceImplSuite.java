package xin.manong.security.keeper.server.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.security.keeper.server.ApplicationTest;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.Tenant;
import xin.manong.security.keeper.model.User;
import xin.manong.security.keeper.model.Vendor;
import xin.manong.security.keeper.server.service.request.UserSearchRequest;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-09-01 15:58:32
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class UserServiceImplSuite {

    @Resource
    protected UserService userService;
    @Resource
    protected TenantService tenantService;
    @Resource
    protected VendorService vendorService;

    @Before
    public void setUp() {
        Vendor vendor = new Vendor();
        vendor.id = "v_abc";
        vendor.name = "test_vendor";
        Assert.assertTrue(vendorService.add(vendor));
        Tenant tenant = new Tenant();
        tenant.id = "t_abc";
        tenant.name = "test_tenant";
        tenant.vendorId = vendor.id;
        Assert.assertTrue(tenantService.add(tenant));
    }

    @After
    public void tearDown() {
        Assert.assertTrue(tenantService.delete("t_abc"));
        Assert.assertTrue(vendorService.delete("v_abc"));
    }

    @Test
    @Transactional
    @Rollback
    public void testUserOperations() {
        {
            User user = new User();
            user.id = "xxx";
            user.userName = "abc";
            user.name = "frankcl";
            user.tenantId = "t_abc";
            user.vendorId = "v_abc";
            user.password = "xxxxxx";
            user.avatar = "http://www.manong.xin/frankcl/avatar.jpg";
            Assert.assertTrue(userService.add(user));
        }
        {
            User user = new User();
            user.id = "xxx";
            user.userName = "abc";
            user.name = "frankcl";
            user.tenantId = "t_abcd";
            user.vendorId = "v_abc";
            user.password = "xxxxxx";
            user.avatar = "http://www.manong.xin/frankcl/avatar.jpg";
            try {
                userService.add(user);
                Assert.assertTrue(false);
            } catch (Exception e) {
            }
        }
        {
            User user = new User();
            user.id = "xxx";
            user.userName = "abc";
            user.name = "frankcl";
            user.tenantId = "t_abc";
            user.vendorId = "v_abcd";
            user.password = "xxxxxx";
            user.avatar = "http://www.manong.xin/frankcl/avatar.jpg";
            try {
                userService.add(user);
                Assert.assertTrue(false);
            } catch (Exception e) {
            }
        }
        {
            User user = new User();
            user.id = "xxx";
            user.userName = "abcd";
            user.name = "frankcl";
            user.tenantId = "t_abc";
            user.vendorId = "v_abc";
            user.password = "xxxxxx";
            user.avatar = "http://www.manong.xin/frankcl/avatar.jpg";
            try {
                userService.add(user);
                Assert.assertTrue(false);
            } catch (Exception e) {
            }
        }
        {
            User user = new User();
            user.id = "xxxx";
            user.userName = "abc";
            user.name = "frankcl";
            user.tenantId = "t_abc";
            user.vendorId = "v_abc";
            user.password = "xxxxxx";
            user.avatar = "http://www.manong.xin/frankcl/avatar.jpg";
            try {
                userService.add(user);
                Assert.assertTrue(false);
            } catch (Exception e) {
            }
        }
        {
            User user = userService.get("xxx");
            Assert.assertTrue(user != null);
            Assert.assertEquals("xxx", user.id);
            Assert.assertEquals("abc", user.userName);
            Assert.assertEquals("frankcl", user.name);
            Assert.assertEquals("t_abc", user.tenantId);
            Assert.assertEquals("v_abc", user.vendorId);
            Assert.assertEquals("dad3a37aa9d50688b5157698acfd7aee", user.password);
            Assert.assertEquals("http://www.manong.xin/frankcl/avatar.jpg", user.avatar);
            Assert.assertTrue(user.createTime > 0);
            Assert.assertTrue(user.updateTime > 0);
        }
        {
            User user = new User();
            user.id = "xxx";
            user.password = "xxxxxxx";
            user.avatar = "http://www.manong.xin/frankcl/avatar1.jpg";
            Assert.assertTrue(userService.update(user));
            Assert.assertTrue(userService.addRole("xxx", "123"));
            Assert.assertTrue(userService.addRole("xxx", "456"));
            Assert.assertTrue(userService.removeRole("xxx", "123"));
        }
        {
            User user = userService.get("xxx");
            Assert.assertTrue(user != null);
            Assert.assertEquals("xxx", user.id);
            Assert.assertEquals("abc", user.userName);
            Assert.assertEquals("frankcl", user.name);
            Assert.assertEquals("t_abc", user.tenantId);
            Assert.assertEquals("v_abc", user.vendorId);
            Assert.assertEquals("04adb4e2f055c978c9bb101ee1bc5cd4", user.password);
            Assert.assertEquals("http://www.manong.xin/frankcl/avatar1.jpg", user.avatar);
            Assert.assertTrue(user.createTime > 0);
            Assert.assertTrue(user.updateTime > 0);
            Assert.assertTrue(user.roles != null && user.roles.size() == 1);
            Assert.assertEquals("456", user.roles.get(0));
        }
        {
            UserSearchRequest searchRequest = new UserSearchRequest();
            searchRequest.tenantId = "t_abc";
            searchRequest.vendorId = "v_abc";
            searchRequest.userName = "abc";
            searchRequest.name = "frank";
            Pager<User> pager = userService.search(searchRequest);
            Assert.assertTrue(pager != null && pager.total == 1 && pager.records.size() == 1);
        }
        {
            Assert.assertTrue(userService.delete("xxx"));
        }
    }
}
