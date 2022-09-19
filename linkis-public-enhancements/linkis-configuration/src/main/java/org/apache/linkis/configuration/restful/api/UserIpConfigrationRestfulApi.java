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
import org.apache.linkis.configuration.entity.UserIpVo;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.configuration.service.UserIpConfigService;
import org.apache.linkis.configuration.util.CommonUtils;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Api(tags = "user ip configuration")
@RestController
@RequestMapping(path = "/configuration/userIpConfig")
public class UserIpConfigrationRestfulApi {

    private static final Logger logger = LoggerFactory.getLogger(UserIpConfigrationRestfulApi.class);

    @Autowired
    private UserIpConfigService userIpConfigService;

    @RequestMapping(path = "/createUserIP", method = RequestMethod.POST)
    public Message createUserIP(HttpServletRequest req, @RequestBody UserIpVo userIpVo) {
        String userName = ModuleUserUtils.getOperationUser(req, "createUserIP");
        if (!Configuration.isAdmin(userName)) {
            return Message.error("Only administrators can configure ");
        }
        try {
            userIpConfigService.createUserIP(userIpVo);
        } catch (DuplicateKeyException e) {
            return Message.error("create user-creator is existed");
        } catch (ConfigurationException e) {
            return Message.error(e.getMessage());
        }
        return Message.ok();
    }

    @RequestMapping(path = "/updateUserIP", method = RequestMethod.POST)
    public Message updateUserIP(HttpServletRequest req, @RequestBody UserIpVo UserIpVo) {
        String userName = ModuleUserUtils.getOperationUser(req, "updateUserIP");
        if (!Configuration.isAdmin(userName)) {
            return Message.error("Only administrators can configure ");
        }
        try {
            userIpConfigService.updateUserIP(UserIpVo);
        } catch (ConfigurationException e) {
            return Message.error(e.getMessage());
        }
        return Message.ok();
    }

    @RequestMapping(path = "/deleteUserIP", method = RequestMethod.GET)
    public Message deleteUserIP(HttpServletRequest req, Integer id) {
        String userName = ModuleUserUtils.getOperationUser(req, "deleteUserIP");
        if (!Configuration.isAdmin(userName)) {
            return Message.error("Only administrators can configure ");
        }
        userIpConfigService.deleteUserIP(id);
        return Message.ok();
    }

    @RequestMapping(path = "/queryUserIPList", method = RequestMethod.GET)
    public Message queryUserIPList(HttpServletRequest req, String user, String creator) {
        String userName = ModuleUserUtils.getOperationUser(req, "queryUserIPList");
        if (!Configuration.isAdmin(userName)) {
            return Message.error("Only administrators can configure ");
        }
        return Message.ok().data("userIpList", userIpConfigService.queryUserIPList());
    }


}
