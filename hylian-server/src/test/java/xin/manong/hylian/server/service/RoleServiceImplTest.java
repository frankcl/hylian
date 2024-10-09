package xin.manong.hylian.server.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.hylian.model.Pager;
import xin.manong.hylian.model.Role;
import xin.manong.hylian.server.ApplicationTest;
import xin.manong.hylian.server.service.request.RoleSearchRequest;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-10-13 15:49:14
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class RoleServiceImplTest {

    @Resource
    protected RoleService roleService;

    @Test
    @Transactional
    @Rollback
    public void testRoleOperations() {
        {
            Role role = new Role();
            role.id = "xxx";
            role.name = "测试角色";
            role.appId = "app_xxx";
            Assert.assertTrue(roleService.add(role));
        }
        {
            Role role = roleService.get("xxx");
            Assert.assertNotNull(role);
            Assert.assertEquals("测试角色", role.name);
            Assert.assertEquals("app_xxx", role.appId);
            Assert.assertNotNull(role.createTime);
            Assert.assertNotNull(role.updateTime);
        }
        {
            Role role = new Role();
            role.id = "xxx";
            role.name = "测试角色1";
            Assert.assertTrue(roleService.update(role));
        }
        {
            Role role = roleService.get("xxx");
            Assert.assertNotNull(role);
            Assert.assertEquals("测试角色1", role.name);
            Assert.assertEquals("app_xxx", role.appId);
            Assert.assertNotNull(role.createTime);
            Assert.assertNotNull(role.updateTime);
        }
        {
            RoleSearchRequest searchRequest = new RoleSearchRequest();
            searchRequest.appId = "app_xxx";
            searchRequest.name = "测试";
            Pager<Role> pager = roleService.search(searchRequest);
            Assert.assertTrue(pager != null && pager.records.size() == 1);
        }
        {
            Assert.assertTrue(roleService.delete("xxx"));
        }
    }
}
