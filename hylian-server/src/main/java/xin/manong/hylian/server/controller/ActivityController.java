package xin.manong.hylian.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.hylian.model.Activity;
import xin.manong.hylian.model.App;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.model.User;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.controller.response.ViewActivity;
import xin.manong.hylian.server.controller.response.ViewUser;
import xin.manong.hylian.server.service.ActivityService;
import xin.manong.hylian.server.service.AppService;
import xin.manong.hylian.server.service.UserService;
import xin.manong.hylian.server.service.request.ActivitySearchRequest;
import xin.manong.weapon.spring.web.ws.aspect.EnableWebLogAspect;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

/**
 * 活动记录控制器
 *
 * @author frankcl
 * @date 2023-09-05 11:58:49
 */
@RestController
@Controller
@Path("api/activity")
@RequestMapping("api/activity")
public class ActivityController {

    @Resource
    protected ActivityService activityService;
    @Resource
    protected AppService appService;
    @Resource
    protected UserService userService;

    /**
     * 搜索登录应用
     *
     * @param searchRequest 搜索请求
     * @return 登录应用分页列表
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    @GetMapping("search")
    @EnableWebLogAspect
    public Pager<ViewActivity> search(@BeanParam ActivitySearchRequest searchRequest) {
        Pager<Activity> pager = activityService.search(searchRequest);
        Pager<ViewActivity> viewPager = new Pager<>();
        viewPager.current = pager.current;
        viewPager.size = pager.size;
        viewPager.total = pager.total;
        viewPager.records = new ArrayList<>();
        for (Activity record : pager.records) viewPager.records.add(fillAndConvert(record));
        return viewPager;
    }

    /**
     * 填充和转换活动记录
     *
     * @param record 活动记录
     * @return 视图层活动记录
     */
    private ViewActivity fillAndConvert(Activity record) {
        App app = appService.get(record.appId);
        if (app == null) throw new NotFoundException("应用不存在");
        User user = userService.get(record.userId);
        if (user == null) throw new NotFoundException("用户不存在");
        ViewUser viewUser = Converter.convert(user, null);
        return Converter.convert(record, app, viewUser);
    }
}
