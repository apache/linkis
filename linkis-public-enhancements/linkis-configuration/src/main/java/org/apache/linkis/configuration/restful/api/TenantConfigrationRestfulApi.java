package org.apache.linkis.configuration.restful.api;

import io.swagger.annotations.Api;
import org.apache.commons.collections.MapUtils;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.configuration.entity.TenantVo;
import org.apache.linkis.configuration.service.TenantConfigService;
import org.apache.linkis.configuration.util.HttpsUtil;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Api(tags = "tenant label  configuration")
@RestController
@RequestMapping(path = "/tenantconfig")
public class TenantConfigrationRestfulApi {

    private static final Logger logger = LoggerFactory.getLogger(TenantConfigrationRestfulApi.class);

    @Autowired
    private TenantConfigService tenantConfigService;

    @RequestMapping(path = "/createTenant", method = RequestMethod.POST)
    public Message createTenant(HttpServletRequest req, @RequestBody TenantVo tenantVo) {
        String userName = ModuleUserUtils.getOperationUser(req, "createTenant");
        if (!Configuration.isAdmin(userName)) {
            return Message.error("Only administrators can configure ");
        }
        return   tenantConfigService.createTenant(tenantVo);
    }

    @RequestMapping(path = "/updateTenant", method = RequestMethod.POST)
    public Message updateTenant(HttpServletRequest req, @RequestBody TenantVo tenantVo) {
        String userName = ModuleUserUtils.getOperationUser(req, "updateTenant");
        if (!Configuration.isAdmin(userName)) {
            return Message.error("Only administrators can configure ");
        }
        return  tenantConfigService.updateTenant(tenantVo);
    }

    @RequestMapping(path = "/deleteTenant", method = RequestMethod.GET)
    public Message deleteTenant(HttpServletRequest req, Integer id) {
        String userName = ModuleUserUtils.getOperationUser(req, "deleteTenant");
        if (!Configuration.isAdmin(userName)) {
            return Message.error("Only administrators can configure ");
        }
        tenantConfigService.deleteTenant(id);
        return Message.ok();
    }

    @RequestMapping(path = "/queryTenantList", method = RequestMethod.GET)
    public Message queryTenantList(HttpServletRequest req,String user,String creator,String tenant) {
        String userName = ModuleUserUtils.getOperationUser(req, "queryTenantList");
        if (!Configuration.isAdmin(userName)) {
            return Message.error("Only administrators can configure ");
        }
        return Message.ok().data("tenantLIst",tenantConfigService.queryTenantList(user,creator,tenant));
    }
}
