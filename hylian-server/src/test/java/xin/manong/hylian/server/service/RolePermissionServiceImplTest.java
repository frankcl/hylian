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
import xin.manong.hylian.model.RolePermission;
import xin.manong.hylian.server.ApplicationTest;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.service.request.RolePermissionSearchRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author frankcl
 * @date 2023-10-16 14:25:28
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class RolePermissionServiceImplTest {

    @Resource
    protected RolePermissionService rolePermissionService;

    @Test
    @Transactional
    @Rollback
    public void testRolePermissionOperations() {
        List<Long> ids = new ArrayList<>();
        {
            RolePermission rolePermission = new RolePermission();
            rolePermission.roleId = "role_xxx";
            rolePermission.permissionId = "permission_xxx";
            Assert.assertTrue(rolePermissionService.add(rolePermission));
            ids.add(rolePermission.id);
        }
        {
            RolePermission rolePermission = new RolePermission();
            rolePermission.roleId = "role_xxx";
            rolePermission.permissionId = "permission_yyy";
            Assert.assertTrue(rolePermissionService.add(rolePermission));
            ids.add(rolePermission.id);
        }
        {
            RolePermission rolePermission = new RolePermission();
            rolePermission.roleId = "role_yyy";
            rolePermission.permissionId = "permission_xxx";
            Assert.assertTrue(rolePermissionService.add(rolePermission));
            ids.add(rolePermission.id);
        }
        {
            RolePermissionSearchRequest searchRequest = new RolePermissionSearchRequest();
            searchRequest.roleIds = new ArrayList<>();
            searchRequest.roleIds.add("role_xxx");
            Pager<RolePermission> pager = rolePermissionService.search(searchRequest);
            Assert.assertNotNull(pager);
            Assert.assertEquals(2L, pager.total.longValue());
            Assert.assertEquals(Constants.DEFAULT_PAGE_SIZE, pager.pageSize.longValue());
            Assert.assertEquals(2, pager.records.size());
            Assert.assertEquals("role_xxx", pager.records.get(0).roleId);
            Assert.assertEquals("permission_yyy", pager.records.get(0).permissionId);
            Assert.assertEquals("role_xxx", pager.records.get(1).roleId);
            Assert.assertEquals("permission_xxx", pager.records.get(1).permissionId);
        }
        {
            RolePermissionSearchRequest searchRequest = new RolePermissionSearchRequest();
            searchRequest.permissionId = "permission_xxx";
            Pager<RolePermission> pager = rolePermissionService.search(searchRequest);
            Assert.assertNotNull(pager);
            Assert.assertEquals(2L, pager.total.longValue());
            Assert.assertEquals(Constants.DEFAULT_PAGE_SIZE, pager.pageSize.longValue());
            Assert.assertEquals(2, pager.records.size());
            Assert.assertEquals("role_yyy", pager.records.get(0).roleId);
            Assert.assertEquals("permission_xxx", pager.records.get(0).permissionId);
            Assert.assertEquals("role_xxx", pager.records.get(1).roleId);
            Assert.assertEquals("permission_xxx", pager.records.get(1).permissionId);
        }
        {
            for (Long id : ids) Assert.assertTrue(rolePermissionService.delete(id));
        }
    }
}
