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
import xin.manong.hylian.model.Pager;
import xin.manong.hylian.model.Tenant;
import xin.manong.hylian.model.Vendor;
import xin.manong.hylian.server.service.request.TenantSearchRequest;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-09-01 15:29:16
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class TenantServiceImplSuite {

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
    }

    @After
    public void tearDown() {
        Assert.assertTrue(vendorService.delete("v_abc"));
    }

    @Test
    @Transactional
    @Rollback
    public void testTenantOperations() {
        {
            Tenant tenant = new Tenant();
            tenant.id = "xxx";
            tenant.name = "test";
            tenant.vendorId = "v_abc";
            Assert.assertTrue(tenantService.add(tenant));
        }
        {
            Tenant tenant = new Tenant();
            tenant.id = "xxx";
            tenant.name = "test";
            tenant.vendorId = "v_xxx";
            try {
                tenantService.add(tenant);
                Assert.fail();
            } catch (Exception e) {
            }
        }
        {
            Tenant tenant = new Tenant();
            tenant.id = "xxx";
            tenant.name = "test1";
            tenant.vendorId = "v_abc";
            try {
                tenantService.add(tenant);
                Assert.fail();
            } catch (Exception e) {
            }
        }
        {
            Tenant tenant = new Tenant();
            tenant.id = "xxxx";
            tenant.name = "test";
            tenant.vendorId = "v_abc";
            try {
                tenantService.add(tenant);
                Assert.fail();
            } catch (Exception e) {
            }
        }
        {
            Tenant tenant = tenantService.get("xxx");
            Assert.assertNotNull(tenant);
            Assert.assertEquals("xxx", tenant.id);
            Assert.assertEquals("test", tenant.name);
            Assert.assertEquals("v_abc", tenant.vendorId);
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
            Assert.assertEquals("v_abc", tenant.vendorId);
            Assert.assertTrue(tenant.createTime > 0);
            Assert.assertTrue(tenant.updateTime > 0);
        }
        {
            TenantSearchRequest searchRequest = new TenantSearchRequest();
            searchRequest.vendorId = "v_abc";
            searchRequest.name = "st12";
            Pager<Tenant> pager = tenantService.search(searchRequest);
            Assert.assertTrue(pager != null && pager.total == 1 && pager.records.size() == 1);
        }
        {
            Assert.assertTrue(tenantService.delete("xxx"));
        }
    }
}
