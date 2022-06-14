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

package org.apache.linkis.manager.am.restful;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;
import com.github.xiaoymin.knife4j.annotations.DynamicResponseParameters;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.manager.am.exception.AMErrorException;
import org.apache.linkis.manager.am.service.ECResourceInfoService;
import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@Api(tags = "EC资源信息管理")
@RequestMapping(
        path = "/linkisManager/ecinfo",
        produces = {"application/json"})
@RestController
public class ECResourceInfoRestfulApi {
    @Autowired private ECResourceInfoService ecResourceInfoService;

    @ApiOperation(value="获取资源清单",notes="查看资源信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="ticketid",value="资源ID",required=true,paramType="query",dataType="String")
    })
    @RequestMapping(path = "/get", method = RequestMethod.GET)
    public Message getECInfo(
            HttpServletRequest req, @RequestParam(value = "ticketid") String ticketid)
            throws AMErrorException {
        String userName = ModuleUserUtils.getOperationUser(req, "getECInfo");
        ECResourceInfoRecord ecResourceInfoRecord =
                ecResourceInfoService.getECResourceInfoRecord(ticketid);
        if (null != ecResourceInfoRecord
                && (userName.equalsIgnoreCase(ecResourceInfoRecord.getCreateUser())
                        || Configuration.isAdmin(userName))) {
            return Message.ok().data("ecResourceInfoRecord", ecResourceInfoRecord);
        } else {
            return Message.error("tickedId not exist:" + ticketid);
        }
    }

    @ApiOperation(value="删除资源",notes="根据资源ID删除指定资源")
    @ApiImplicitParams({
            @ApiImplicitParam(name="ticketid",value="资源ID",required=true,paramType="query",dataType="String")
    })
    @RequestMapping(path = "/delete/{ticketid}}", method = RequestMethod.DELETE)
    public Message deleteECInfo(HttpServletRequest req, @PathVariable("ticketid") String ticketid)
            throws AMErrorException {
        String userName = ModuleUserUtils.getOperationUser(req, "deleteECInfo");
        ECResourceInfoRecord ecResourceInfoRecord =
                ecResourceInfoService.getECResourceInfoRecord(ticketid);
        if (null != ecResourceInfoRecord
                && (userName.equalsIgnoreCase(ecResourceInfoRecord.getCreateUser())
                        || Configuration.isAdmin(userName))) {
            ecResourceInfoService.deleteECResourceInfoRecord(ecResourceInfoRecord.getId());
            return Message.ok().data("ecResourceInfoRecord", ecResourceInfoRecord);
        } else {
            return Message.error("tickedId not exist:" + ticketid);
        }
    }
}
