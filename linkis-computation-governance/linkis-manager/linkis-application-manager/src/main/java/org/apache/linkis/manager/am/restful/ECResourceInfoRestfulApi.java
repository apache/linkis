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

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.governance.common.constant.job.JobRequestConstants;
import org.apache.linkis.manager.am.exception.AMErrorException;
import org.apache.linkis.manager.am.service.ECResourceInfoService;
import org.apache.linkis.manager.am.util.ECResourceInfoUtils;
import org.apache.linkis.manager.am.vo.ECResourceInfoRecordVo;
import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.lang.StringUtils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.*;

/*@Api(tags = "EC资源信息管理")*/
@Api(tags = "EC_Resource_Information_Management")
@RequestMapping(
        path = "/linkisManager/ecinfo",
        produces = {"application/json"})
@RestController
public class ECResourceInfoRestfulApi {
    @Autowired private ECResourceInfoService ecResourceInfoService;

    /*@ApiOperation(value = "获取EC信息", notes = "获取EC信息", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "ticketid", required = true, dataType = "String", value = "ticketid")
    })*/
    @ApiOperation(value = "GetECInfo", notes = "Get_Ec_Info", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "ticketid",
                required = true,
                dataType = "String",
                value = "Ticket_Id")
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

    /*@ApiOperation(value = "删除EC信息", notes = "删除EC信息", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "ticketid", required = true, dataType = "String", value = "Ticket_Id")
    })*/
    @ApiOperation(value = "DeleteECInfo", notes = "Delete_Ec_Info", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "ticketid",
                required = true,
                dataType = "String",
                value = "Ticket_Id")
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

    /*@ApiOperation(value = "ecr历史列表", notes = "ecr历史列表", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "instance", required = false, dataType = "String", value = "instance"),
        @ApiImplicitParam(name = "creator", required = false, dataType = "String", value = "creator")
    })*/
    @ApiOperation(value = "QueryEcrHistory", notes = "Query_Ecr_History", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "instance", dataType = "String", value = "Instance"),
        @ApiImplicitParam(name = "creator", dataType = "String", value = "Creator")
    })
    @RequestMapping(path = "/ecrHistoryList", method = RequestMethod.GET)
    public Message queryEcrHistory(
            HttpServletRequest req,
            @RequestParam(value = "instance", required = false) String instance,
            @RequestParam(value = "creator", required = false) String creator,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                    @RequestParam(value = "startDate", required = false)
                    Date startDate,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                    @RequestParam(
                            value = "endDate",
                            required = false,
                            defaultValue = "#{new java.util.Date()}")
                    Date endDate,
            @RequestParam(value = "engineType", required = false) String engineType,
            @RequestParam(value = "pageNow", required = false, defaultValue = "1") Integer pageNow,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20")
                    Integer pageSize) {
        String username = SecurityFilter.getLoginUsername(req);
        // Parameter judgment
        instance = ECResourceInfoUtils.strCheckAndDef(instance, null);
        String creatorUser = ECResourceInfoUtils.strCheckAndDef(creator, null);
        engineType = ECResourceInfoUtils.strCheckAndDef(engineType, null);
        if (null != creatorUser && !ECResourceInfoUtils.checkNameValid(creatorUser)) {
            return Message.error("Invalid creator : " + creatorUser);
        }
        if (null == startDate) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            startDate = calendar.getTime();
        }
        if (Configuration.isAdmin(username)) {
            username = null;
            if (StringUtils.isNotBlank(creatorUser)) {
                username = creatorUser;
            }
        }
        List<ECResourceInfoRecordVo> list = new ArrayList<>();
        List<ECResourceInfoRecord> queryTasks = null;

        PageHelper.startPage(pageNow, pageSize);
        try {
            queryTasks =
                    ecResourceInfoService.getECResourceInfoRecordList(
                            instance, endDate, startDate, username, engineType);
            queryTasks.forEach(
                    info -> {
                        ECResourceInfoRecordVo ecrHistroryListVo = new ECResourceInfoRecordVo();
                        BeanUtils.copyProperties(info, ecrHistroryListVo);
                        ecrHistroryListVo.setEngineType(
                                info.getLabelValue().split(",")[1].split("-")[0]);
                        ecrHistroryListVo.setUsedResource(
                                ECResourceInfoUtils.getStringToMap(info.getUsedResource(), info));
                        ecrHistroryListVo.setReleasedResource(
                                ECResourceInfoUtils.getStringToMap(
                                        info.getReleasedResource(), info));
                        ecrHistroryListVo.setRequestResource(
                                ECResourceInfoUtils.getStringToMap(
                                        info.getRequestResource(), info));
                        list.add(ecrHistroryListVo);
                    });
        } finally {
            PageHelper.clearPage();
        }
        PageInfo<ECResourceInfoRecord> pageInfo = new PageInfo<>(queryTasks);
        long total = pageInfo.getTotal();
        return Message.ok().data("engineList", list).data(JobRequestConstants.TOTAL_PAGE(), total);
    }
}
