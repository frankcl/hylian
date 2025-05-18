package xin.manong.hylian.server.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.hylian.client.util.SessionUtils;
import xin.manong.hylian.server.service.CaptchaService;

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
    public String create(@Context HttpServletRequest httpRequest) {
        String sessionId = SessionUtils.getSessionID(httpRequest);
        return captchaService.create(sessionId);
    }

}