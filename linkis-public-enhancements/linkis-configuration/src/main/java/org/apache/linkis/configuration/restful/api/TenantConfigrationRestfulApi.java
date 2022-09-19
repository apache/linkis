/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.configuration.restful.api;

import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.configuration.entity.TenantVo;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.configuration.service.TenantConfigService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Api(tags = "tenant label  configuration")
@RestController
@RequestMapping(path = "/configuration/tenantConfig")
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
        try {
            tenantConfigService.createTenant(tenantVo);
        } catch (DuplicateKeyException e) {
            return Message.error("create user-creator is existed");
        } catch (ConfigurationException e) {
            return Message.error(e.getMessage());
        }
        return Message.ok();
    }

    @RequestMapping(path = "/updateTenant", method = RequestMethod.POST)
    public Message updateTenant(HttpServletRequest req, @RequestBody TenantVo tenantVo) {
        String userName = ModuleUserUtils.getOperationUser(req, "updateTenant");
        if (!Configuration.isAdmin(userName)) {
            return Message.error("Only administrators can configure ");
        }
        try {
            tenantConfigService.updateTenant(tenantVo);
        } catch (ConfigurationException e) {
            return Message.error(e.getMessage());
        }
        return Message.ok();
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
    public Message queryTenantList(HttpServletRequest req, String user, String creator, String tenant) {
        String userName = ModuleUserUtils.getOperationUser(req, "queryTenantList");
        if (!Configuration.isAdmin(userName)) {
            return Message.error("Only administrators can configure ");
        }
        return Message.ok().data("tenantLIst", tenantConfigService.queryTenantList(user, creator, tenant));
    }

    @RequestMapping(path = "/checkUserCteator", method = RequestMethod.GET)
    public Message checkUserCteator(HttpServletRequest req, String user, String creator, String tenant) {
        String userName = ModuleUserUtils.getOperationUser(req, "queryTenantList");
        if (!Configuration.isAdmin(userName)) {
            return Message.error("Only administrators can configure ");
        }
        return Message.ok().data("result", tenantConfigService.checkUserCteator(user, creator, tenant));
    }
}
