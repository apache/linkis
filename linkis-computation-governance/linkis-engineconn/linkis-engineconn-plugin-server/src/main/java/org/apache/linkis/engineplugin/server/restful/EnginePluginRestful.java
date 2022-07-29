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

package org.apache.linkis.engineplugin.server.restful;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.engineplugin.server.service.EngineConnResourceService;
import org.apache.linkis.engineplugin.server.service.RefreshEngineConnResourceRequest;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "ECP(engineconn plugin) operation")
@RestController
@RequestMapping(path = "/engineplugin")
public class EnginePluginRestful {

    private static final Logger log = LoggerFactory.getLogger(EnginePluginRestful.class);

    @Autowired private EngineConnResourceService engineConnResourceService;

    @ApiOperation(value = "refreshAll", notes = "refresh all engineconn resource", response = Message.class)
    @RequestMapping(path = "/refreshAll", method = RequestMethod.GET)
    public Message refreshAll(HttpServletRequest req) {
        String username = ModuleUserUtils.getOperationUser(req, "refreshAll");
        if (Configuration.isAdmin(username)) {
            log.info("{} start to refresh all ec resource", username);
            engineConnResourceService.refreshAll(true);
            log.info("{} finished to refresh all ec resource", username);
            return Message.ok().data("msg", "Refresh successfully");
        } else {
            return Message.error("Only administrators can operate");
        }
    }

    @ApiOperation(value = "refreshAll", notes = "refresh one engineconn resource", response = Message.class)
    @RequestMapping(path = "/refresh", method = RequestMethod.GET)
    public Message refreshOne(
            HttpServletRequest req,
            @RequestParam(value = "ecType") String ecType,
            @RequestParam(value = "version", required = false) String version) {
        String username = ModuleUserUtils.getOperationUser(req, "refreshOne");
        if (Configuration.isAdmin(username)) {
            log.info("{} start to refresh {} ec resource", username, ecType);
            RefreshEngineConnResourceRequest refreshEngineConnResourceRequest =
                    new RefreshEngineConnResourceRequest();
            refreshEngineConnResourceRequest.setEngineConnType(ecType);
            refreshEngineConnResourceRequest.setVersion(version);
            engineConnResourceService.refresh(refreshEngineConnResourceRequest);
            log.info("{} finished to refresh {} ec resource", username, ecType);
            return Message.ok().data("msg", "Refresh successfully");
        } else {
            return Message.error("Only administrators can operate");
        }
    }
}
