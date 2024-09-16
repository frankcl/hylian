package xin.manong.security.keeper.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.manong.security.keeper.model.AppLogin;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.server.service.AppLoginService;
import xin.manong.security.keeper.server.service.request.AppLoginSearchRequest;
import xin.manong.weapon.spring.web.ws.aspect.EnableWebLogAspect;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * 登录用户应用控制器
 *
 * @author frankcl
 * @date 2023-09-05 11:58:49
 */
@RestController
@Controller
@Path("api/appLogin")
@RequestMapping("api/appLogin")
public class AppLoginController {

    @Resource
    protected AppLoginService appLoginService;

    /**
     * 搜索登录应用
     *
     * @param searchRequest 搜索请求
     * @return 登录应用分页列表
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    @PostMapping("search")
    @EnableWebLogAspect
    public Pager<AppLogin> search(@RequestBody AppLoginSearchRequest searchRequest) {
        return appLoginService.search(searchRequest);
    }
}
