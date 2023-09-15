package xin.manong.security.keeper.server.controller;

import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.server.mvc.Viewable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xin.manong.security.keeper.common.util.SessionUtils;
import xin.manong.security.keeper.model.Tenant;
import xin.manong.security.keeper.model.User;
import xin.manong.security.keeper.model.Vendor;
import xin.manong.security.keeper.server.common.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * 视图控制器
 *
 * @author frankcl
 * @date 2023-09-08 18:15:00
 */
@RestController
@Controller
@Path("/home")
@RequestMapping("/home")
public class ViewController {

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("index")
    @GetMapping("index")
    @Template(name = "/index.ftl")
    public Viewable index(@Context HttpServletRequest httpRequest) {
        User user = SessionUtils.getUser(httpRequest);
        Tenant tenant = SessionUtils.getTenant(httpRequest);
        Vendor vendor = SessionUtils.getVendor(httpRequest);
        Map<String, Object> viewModel = new HashMap<>();
        viewModel.put(Constants.PARAM_LOGOUT_URL, Constants.PATH_LOGOUT);
        if (user != null) {
            viewModel.put(Constants.PARAM_USER_NAME, user.userName);
            viewModel.put(Constants.PARAM_NAME, user.name);
        }
        if (tenant != null) {
            viewModel.put(Constants.PARAM_TENANT, tenant.name);
        }
        if (vendor != null) {
            viewModel.put(Constants.PARAM_VENDOR, vendor.name);
        }
        return new Viewable("/index", viewModel);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("login")
    @GetMapping("login")
    @Template(name = "/login.ftl")
    public Map<String, Object> login(@QueryParam("redirect_url") @RequestParam("redirect_url") String redirectURL) {
        Map<String, Object> viewModel = new HashMap<>();
        viewModel.put(Constants.PARAM_REDIRECT_URL, redirectURL);
        return viewModel;
    }
}
