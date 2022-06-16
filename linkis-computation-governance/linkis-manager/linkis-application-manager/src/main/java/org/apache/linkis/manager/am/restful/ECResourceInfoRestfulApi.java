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

import com.github.pagehelper.PageHelper;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.manager.am.exception.AMErrorException;
import org.apache.linkis.manager.am.service.ECResourceInfoService;
import org.apache.linkis.manager.am.util.QueryUtils;
import org.apache.linkis.manager.am.util.StrUtils;
import org.apache.linkis.manager.am.vo.ECRHistroryListVo;
import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.linkis.server.utils.ModuleUserUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping(
        path = "/linkisManager/ecinfo",
        produces = {"application/json"})
@RestController
public class ECResourceInfoRestfulApi {
    @Autowired private ECResourceInfoService ecResourceInfoService;

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

    @RequestMapping(path = "/ecrHistoryList", method = RequestMethod.GET)
    public Message queryEcrHistory(HttpServletRequest req,
                                   @RequestParam(value = "instance", required = false) String instance,
                                   @RequestParam(value = "creator", required = false) String creator,
                                   @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                   @RequestParam(value = "startDate", required = false) Date startDate,
                                   @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                   @RequestParam(value = "endDate", required = false,defaultValue = "#{new java.util.Date()}") Date endDate,
                                   @RequestParam(value = "engineType", required = false) String engineType,
                                   @RequestParam(value = "pageNow", required = false,defaultValue = "1") Integer pageNow,
                                   @RequestParam(value = "pageSize", required = false,defaultValue = "20") Integer pageSize) {
        String username = SecurityFilter.getLoginUsername(req);
        // Parameter judgment
        instance    = StrUtils.strCheckAndDef(instance,null);
        creator     = StrUtils.strCheckAndDef(creator,null);
        engineType  = StrUtils.strCheckAndDef(engineType,null);
        if ( null!=creator&&!QueryUtils.checkNameValid(creator)){
            return Message.error("Invalid creator : " + creator);
        }
        if ( null == startDate) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY,0);
            calendar.set(Calendar.MINUTE,0);
            calendar.set(Calendar.SECOND,0);
            startDate =calendar.getTime() ;
        }
        if (Configuration.isAdmin(username)){
            username = null;
            if (!StringUtils.isEmpty(creator)){
                username = creator ;
            }
        }
        List<ECRHistroryListVo> list =  new ArrayList<>();
        PageHelper.startPage(pageNow, pageSize);
        try {
            List<ECResourceInfoRecord> queryTasks = ecResourceInfoService.getECResourceInfoRecordList(instance,endDate,startDate,username);
            if (!StringUtils.isEmpty(engineType)){
                String finalEngineType = engineType;
                queryTasks = queryTasks.stream().filter(info->info.getLabelValue().contains(finalEngineType)).collect(Collectors.toList());
            }
            queryTasks.forEach(info ->{
                ECRHistroryListVo ecrHistroryListVo = new ECRHistroryListVo();
                BeanUtils.copyProperties(info,ecrHistroryListVo);
                ecrHistroryListVo.setEngineType(info.getLabelValue().split(",")[1].split("-")[0]);
                list.add(ecrHistroryListVo);
            });
        } finally {
            PageHelper.clearPage();
        }
        return  Message.ok().data("engineList", list);
    }
}
