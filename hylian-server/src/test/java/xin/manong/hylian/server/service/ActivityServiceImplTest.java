package xin.manong.hylian.server.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.hylian.model.Pager;
import xin.manong.hylian.server.ApplicationTest;
import xin.manong.hylian.model.Activity;
import xin.manong.hylian.server.service.request.ActivitySearchRequest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author frankcl
 * @date 2023-09-04 11:18:50
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class ActivityServiceImplTest {

    @Resource
    protected ActivityService activityService;

    @Test
    @Transactional
    @Rollback
    public void testAppLoginOperations() {
        {
            Activity activity = new Activity();
            activity.setTicketId("xxx").setUserId("user1").setSessionId("session1").
                    setAppId("app1");
            Assert.assertTrue(activityService.add(activity));
        }
        {
            Activity activity = new Activity();
            activity.setTicketId("xxx").setUserId("user1").setSessionId("session2").
                    setAppId("app2");
            Assert.assertTrue(activityService.add(activity));
        }
        {
            Assert.assertTrue(activityService.isCheckin("app1", "session1"));
        }
        {
            Assert.assertTrue(activityService.isCheckin("app2", "session2"));
        }
        {
            Assert.assertFalse(activityService.isCheckin("app3", "session3"));
        }
        {
            List<Activity> activities = activityService.getWithTicket("xxx");
            Assert.assertTrue(activities != null && activities.size() == 2);
            Assert.assertEquals("xxx", activities.get(0).ticketId);
            Assert.assertEquals("user1", activities.get(0).userId);
            Assert.assertEquals("app1", activities.get(0).appId);
            Assert.assertEquals("session1", activities.get(0).sessionId);
            Assert.assertTrue(activities.get(0).createTime > 0);
            Assert.assertTrue(activities.get(0).updateTime > 0);

            Assert.assertEquals("xxx", activities.get(1).ticketId);
            Assert.assertEquals("user1", activities.get(1).userId);
            Assert.assertEquals("app2", activities.get(1).appId);
            Assert.assertEquals("session2", activities.get(1).sessionId);
            Assert.assertTrue(activities.get(1).createTime > 0);
            Assert.assertTrue(activities.get(1).updateTime > 0);
        }
        {
            ActivitySearchRequest searchRequest = new ActivitySearchRequest();
            searchRequest.appId = "app1";
            searchRequest.userId = "user1";
            Pager<Activity> pager = activityService.search(searchRequest);
            Assert.assertTrue(pager != null && pager.total == 1 && pager.records.size() == 1);
        }
        {
            activityService.remove("xxx");
            List<Activity> activities = activityService.getWithTicket("xxx");
            Assert.assertTrue(activities == null || activities.isEmpty());
        }
    }
}
