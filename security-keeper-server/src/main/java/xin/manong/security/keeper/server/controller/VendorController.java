package xin.manong.security.keeper.server.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.Vendor;
import xin.manong.security.keeper.server.service.VendorService;
import xin.manong.security.keeper.server.service.request.VendorSearchRequest;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 供应商控制器
 *
 * @author frankcl
 * @date 2023-09-05 11:44:05
 */
@RestController
@Controller
@Path("/vendor")
@RequestMapping("/vendor")
public class VendorController {

    private static final Logger logger = LoggerFactory.getLogger(VendorController.class);

    @Resource
    protected VendorService vendorService;

    /**
     * 获取供应商信息
     *
     * @param id 供应商ID
     * @return 供应商信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    public Vendor get(@QueryParam("id")  String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("vendor id is empty");
            throw new BadRequestException("供应商ID为空");
        }
        Vendor vendor = vendorService.get(id);
        if (vendor == null) {
            logger.error("vendor[{}] is not found", id);
            throw new NotFoundException(String.format("供应商[%s]不存在", id));
        }
        return vendor;
    }

    /**
     * 增加供应商信息
     *
     * @param vendor 供应商信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PostMapping("add")
    public boolean add(@RequestBody Vendor vendor) {
        if (vendor == null) {
            logger.error("add vendor is null");
            throw new BadRequestException("增加供应商信息为空");
        }
        vendor.check();
        return vendorService.add(vendor);
    }

    /**
     * 更新供应商信息
     *
     * @param vendor 供应商信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    public boolean update(@RequestBody Vendor vendor) {
        if (vendor == null) {
            logger.error("update vendor is null");
            throw new BadRequestException("更新供应商信息为空");
        }
        if (StringUtils.isEmpty(vendor.id)) {
            logger.error("vendor id is empty");
            throw new BadRequestException("供应商ID为空");
        }
        return vendorService.update(vendor);
    }

    /**
     * 删除供应商信息
     *
     * @param id 供应商ID
     * @return 删除成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @DeleteMapping("delete")
    public boolean delete(@QueryParam("id") String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("vendor id is empty");
            throw new BadRequestException("供应商ID为空");
        }
        return vendorService.delete(id);
    }

    /**
     * 搜索供应商
     *
     * @param searchRequest 搜索请求
     * @return 供应商分页列表
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    @PostMapping("search")
    public Pager<Vendor> search(@RequestBody VendorSearchRequest searchRequest) {
        return vendorService.search(searchRequest);
    }
}
