package xin.manong.hylian.server.service;

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
import xin.manong.hylian.server.service.request.TenantSearchRequest;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-09-01 15:29:16
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class TenantServiceImplTest {

    @Resource
    protected TenantService tenantService;

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test(expected = IllegalStateException.class)
    @Transactional
    @Rollback
    public void testAddSameId() {
        {
            Tenant tenant = new Tenant();
            tenant.id = "xxx";
            tenant.name = "test";
            Assert.assertTrue(tenantService.add(tenant));
        }
        {
            Tenant tenant = new Tenant();
            tenant.id = "xxx";
            tenant.name = "test1";
            tenantService.add(tenant);
        }
    }

    @Test(expected = IllegalStateException.class)
    @Transactional
    @Rollback
    public void testAddSameName() {
        {
            Tenant tenant = new Tenant();
            tenant.id = "xxx";
            tenant.name = "test";
            Assert.assertTrue(tenantService.add(tenant));
        }
        {
            Tenant tenant = new Tenant();
            tenant.id = "xxxx";
            tenant.name = "test";
            tenantService.add(tenant);
        }
    }

    @Test
    @Transactional
    @Rollback
    public void testTenantOperations() {
        {
            Tenant tenant = new Tenant();
            tenant.id = "xxx";
            tenant.name = "test";
            Assert.assertTrue(tenantService.add(tenant));
        }
        {
            Tenant tenant = tenantService.get("xxx");
            Assert.assertNotNull(tenant);
            Assert.assertEquals("xxx", tenant.id);
            Assert.assertEquals("test", tenant.name);
            Assert.assertTrue(tenant.createTime > 0);
            Assert.assertTrue(tenant.updateTime > 0);
        }
        {
            Tenant tenant = new Tenant();
            tenant.id = "xxx";
            tenant.name = "test123";
            Assert.assertTrue(tenantService.update(tenant));
        }
        {
            Tenant tenant = tenantService.get("xxx");
            Assert.assertNotNull(tenant);
            Assert.assertEquals("xxx", tenant.id);
            Assert.assertEquals("test123", tenant.name);
            Assert.assertTrue(tenant.createTime > 0);
            Assert.assertTrue(tenant.updateTime > 0);
        }
        {
            TenantSearchRequest searchRequest = new TenantSearchRequest();
            searchRequest.name = "st12";
            Pager<Tenant> pager = tenantService.search(searchRequest);
            Assert.assertTrue(pager != null && pager.total == 1 && pager.records.size() == 1);
        }
        {
            Assert.assertTrue(tenantService.delete("xxx"));
        }
    }
}
