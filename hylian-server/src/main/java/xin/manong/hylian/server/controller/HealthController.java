package xin.manong.hylian.server.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.hylian.server.component.TicketTokenManagement;

/**
 * 健康检测
 *
 * @author frankcl
 * @date 2022-03-11 14:12:06
 */
@RestController
@Controller
@Path("api/health")
@RequestMapping("api/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    private static final String HEALTH_REPORT = "Server is OK";

    @Resource
    private TicketTokenManagement ticketTokenManagement;

    /**
     * 健康检测
     *
     * @return 响应信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("check")
    @GetMapping("check")
    public String check() {
        return HEALTH_REPORT;
    }

    /**
     * 刷新token
     *
     * @param token 令牌
     * @return 新令牌
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("refreshToken/{token}")
    @PostMapping("refreshToken/{token}")
    public String refreshToken(@PathVariable("token") @PathParam("token") String token) {
        if (StringUtils.isEmpty(token)) throw new BadRequestException("刷新Token为空");
        if (!ticketTokenManagement.verifyToken(token)) throw new NotAuthorizedException("Token验证失败");
        String ticket = ticketTokenManagement.getTicketByToken(token);
        if (StringUtils.isEmpty(ticket)) {
            logger.error("Cached ticket is expired for token:{}", token);
            throw new NotAuthorizedException("缓存Ticket过期");
        }
        ticketTokenManagement.removeTokens(token);
        return ticketTokenManagement.buildToken(ticket);
    }
}
