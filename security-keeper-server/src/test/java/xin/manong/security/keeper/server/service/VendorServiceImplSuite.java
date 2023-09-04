package xin.manong.security.keeper.server.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.security.keeper.server.ApplicationTest;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.Vendor;
import xin.manong.security.keeper.server.service.request.VendorSearchRequest;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-09-01 14:49:33
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class VendorServiceImplSuite {

    @Resource
    protected VendorService vendorService;

    @Test
    @Transactional
    @Rollback
    public void testVendorOperations() {
        {
            Vendor vendor = new Vendor();
            vendor.id = "xxx";
            vendor.name = "test";
            Assert.assertTrue(vendorService.add(vendor));
        }
        {
            Vendor vendor = new Vendor();
            vendor.id = "xxx";
            vendor.name = "test1";
            try {
                Assert.assertFalse(vendorService.add(vendor));
                Assert.assertTrue(false);
            } catch (Exception e) {
            }
        }
        {
            Vendor vendor = new Vendor();
            vendor.id = "xxxx";
            vendor.name = "test";
            try {
                Assert.assertFalse(vendorService.add(vendor));
                Assert.assertTrue(false);
            } catch (Exception e) {
            }
        }
        {
            Vendor vendor = vendorService.get("xxx");
            Assert.assertTrue(vendor != null);
            Assert.assertEquals("xxx", vendor.id);
            Assert.assertEquals("test", vendor.name);
            Assert.assertTrue(vendor.createTime > 0);
            Assert.assertTrue(vendor.updateTime > 0);
        }
        {
            Vendor vendor = new Vendor();
            vendor.id = "xxx";
            vendor.name = "test123";
            Assert.assertTrue(vendorService.update(vendor));
        }
        {
            Vendor vendor = vendorService.get("xxx");
            Assert.assertTrue(vendor != null);
            Assert.assertEquals("xxx", vendor.id);
            Assert.assertEquals("test123", vendor.name);
            Assert.assertTrue(vendor.createTime > 0);
            Assert.assertTrue(vendor.updateTime > 0);
        }
        {
            VendorSearchRequest searchRequest = new VendorSearchRequest();
            searchRequest.name = "test";
            Pager<Vendor> pager = vendorService.search(searchRequest);
            Assert.assertTrue(pager != null && pager.total == 1 && pager.records.size() == 1);
        }
        {
            Assert.assertTrue(vendorService.delete("xxx"));
        }
    }
}
