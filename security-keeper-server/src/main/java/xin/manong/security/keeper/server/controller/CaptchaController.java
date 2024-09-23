package xin.manong.security.keeper.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.security.keeper.common.util.SessionUtils;
import xin.manong.security.keeper.server.service.CaptchaService;
import xin.manong.weapon.spring.web.ws.aspect.EnableWebLogAspect;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * 验证码控制器
 *
 * @author frankcl
 * @date 2024-09-05 11:58:49
 */
@RestController
@Controller
@Path("api/captcha")
@RequestMapping("api/captcha")
public class CaptchaController {

    @Resource
    protected CaptchaService captchaService;

    /**
     * 申请验证码
     *
     * @return 验证码
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("apply")
    @GetMapping("apply")
    @EnableWebLogAspect
    public String create(@Context HttpServletRequest httpRequest) {
        String sessionId = SessionUtils.getSessionID(httpRequest);
        return captchaService.create(sessionId);
    }

}