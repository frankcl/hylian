package xin.manong.hylian.server.service;

import jakarta.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.model.Permission;
import xin.manong.hylian.server.ApplicationTest;
import xin.manong.hylian.server.service.request.PermissionSearchRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author frankcl
 * @date 2023-10-13 14:56:39
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class PermissionServiceImplTest {

    @Resource
    protected PermissionService permissionService;

    @Test
    @Transactional
    @Rollback
    public void testPermissionOperations() {
        {
            Permission permission = new Permission();
            permission.id = "xxx";
            permission.name = "测试权限";
            permission.appId = "app_xxx";
            permission.path = "/**";
            Assert.assertTrue(permissionService.add(permission));
        }
        {
            Permission permission = permissionService.get("xxx");
            Assert.assertNotNull(permission);
            Assert.assertNotNull(permission.getCreateTime());
            Assert.assertNotNull(permission.getUpdateTime());
            Assert.assertEquals("测试权限", permission.name);
            Assert.assertEquals("app_xxx", permission.appId);
            Assert.assertEquals("/**", permission.path);
        }
        {
            Permission permission = new Permission();
            permission.id = "xxx";
            permission.name = "测试权限1";
            permission.path = "/*";
            Assert.assertTrue(permissionService.update(permission));
        }
        {
            Permission permission = permissionService.get("xxx");
            Assert.assertNotNull(permission);
            Assert.assertNotNull(permission.getCreateTime());
            Assert.assertNotNull(permission.getUpdateTime());
            Assert.assertEquals("测试权限1", permission.name);
            Assert.assertEquals("app_xxx", permission.appId);
            Assert.assertEquals("/*", permission.path);
        }
        {
            List<String> ids = new ArrayList<>();
            ids.add("xxx");
            List<Permission> permissions = permissionService.batchGet(ids);
            Assert.assertTrue(permissions != null && permissions.size() == 1);
        }
        {
            PermissionSearchRequest searchRequest = new PermissionSearchRequest();
            searchRequest.appId = "app_xxx";
            searchRequest.name = "测试";
            Pager<Permission> pager = permissionService.search(searchRequest);
            Assert.assertTrue(pager != null && pager.records.size() == 1);
        }
        {
            Assert.assertTrue(permissionService.delete("xxx"));
        }
    }
}
