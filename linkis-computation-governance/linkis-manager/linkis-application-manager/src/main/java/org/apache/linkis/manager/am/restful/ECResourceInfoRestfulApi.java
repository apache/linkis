/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.am.restful;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.utils.JsonUtils;
import org.apache.linkis.governance.common.constant.job.JobRequestConstants;
import org.apache.linkis.manager.am.exception.AMErrorException;
import org.apache.linkis.manager.am.service.ECResourceInfoService;
import org.apache.linkis.manager.am.util.ECResourceInfoUtils;
import org.apache.linkis.manager.am.vo.ECResourceInfoRecordVo;
import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "enginneconn resource info operation")
@RequestMapping(
    path = "/linkisManager/ecinfo",
    produces = {"application/json"})
@RestController
public class ECResourceInfoRestfulApi {

  @Autowired private ECResourceInfoService ecResourceInfoService;

  private static final Logger logger = LoggerFactory.getLogger(ECResourceInfoRestfulApi.class);

  @ApiOperation(value = "get", notes = "get engineconn info ", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "ticketid", required = true, dataType = "String", value = "ticket id")
  })
  @RequestMapping(path = "/get", method = RequestMethod.GET)
  public Message getECInfo(
      HttpServletRequest req, @RequestParam(value = "ticketid") String ticketid)
      throws AMErrorException {
    ECResourceInfoRecord ecResourceInfoRecord =
        ecResourceInfoService.getECResourceInfoRecord(ticketid);
    String userName = ModuleUserUtils.getOperationUser(req, "getECInfo ticketid:") + ticketid;
    if (null != ecResourceInfoRecord
        && (userName.equalsIgnoreCase(ecResourceInfoRecord.getCreateUser())
            || Configuration.isAdmin(userName))) {
      return Message.ok().data("ecResourceInfoRecord", ecResourceInfoRecord);
    } else {
      return Message.error("tickedId not exist:" + ticketid);
    }
  }

  @ApiOperation(value = "delete", notes = "delete engineconn info", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "ticketid", required = true, dataType = "String", value = "ticket id")
  })
  @RequestMapping(path = "/delete/{ticketid}}", method = RequestMethod.DELETE)
  public Message deleteECInfo(HttpServletRequest req, @PathVariable("ticketid") String ticketid)
      throws AMErrorException {
    ECResourceInfoRecord ecResourceInfoRecord =
        ecResourceInfoService.getECResourceInfoRecord(ticketid);
    String userName = ModuleUserUtils.getOperationUser(req, "deleteECInfo ticketid:" + ticketid);
    if (null != ecResourceInfoRecord
        && (userName.equalsIgnoreCase(ecResourceInfoRecord.getCreateUser())
            || Configuration.isAdmin(userName))) {
      ecResourceInfoService.deleteECResourceInfoRecord(ecResourceInfoRecord.getId());
      return Message.ok().data("ecResourceInfoRecord", ecResourceInfoRecord);
    } else {
      return Message.error("tickedId not exist:" + ticketid);
    }
  }

  @ApiOperation(
      value = "ecrHistoryList",
      notes = "query engineconn resource history info list",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "instance", dataType = "String", value = "instance"),
    @ApiImplicitParam(name = "creator", dataType = "String", value = "creator"),
    @ApiImplicitParam(name = "startDate", dataType = "String", value = "start date"),
    @ApiImplicitParam(name = "endDate", dataType = "String", value = "end date"),
    @ApiImplicitParam(name = "engineType", dataType = "String", value = "engine type"),
    @ApiImplicitParam(name = "pageNow", dataType = "String", value = "page now"),
    @ApiImplicitParam(name = "pageSize", dataType = "String", value = "page size")
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
      @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize) {
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
            ecrHistroryListVo.setEngineType(info.getLabelValue().split(",")[1].split("-")[0]);
            ecrHistroryListVo.setUsedResource(
                ECResourceInfoUtils.getStringToMap(info.getUsedResource(), info));
            ecrHistroryListVo.setReleasedResource(
                ECResourceInfoUtils.getStringToMap(info.getReleasedResource(), info));
            ecrHistroryListVo.setRequestResource(
                ECResourceInfoUtils.getStringToMap(info.getRequestResource(), info));
            list.add(ecrHistroryListVo);
          });
    } finally {
      PageHelper.clearPage();
    }
    PageInfo<ECResourceInfoRecord> pageInfo = new PageInfo<>(queryTasks);
    long total = pageInfo.getTotal();
    return Message.ok().data("engineList", list).data(JobRequestConstants.TOTAL_PAGE(), total);
  }

  @ApiOperation(value = "ecList", notes = "query engineconn info list", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "creators", dataType = "Array", required = true, value = "creators"),
    @ApiImplicitParam(name = "engineTypes", dataType = "Array", value = "engine type"),
    @ApiImplicitParam(name = "statuss", dataType = "Array", value = "statuss"),
  })
  @RequestMapping(path = "/ecList", method = RequestMethod.POST)
  public Message queryEcList(HttpServletRequest req, @RequestBody JsonNode jsonNode) {

    JsonNode creatorsParam = jsonNode.get("creators");
    JsonNode engineTypesParam = jsonNode.get("engineTypes");
    JsonNode statussParam = jsonNode.get("statuss");

    if (creatorsParam == null || creatorsParam.isNull() || creatorsParam.size() == 0) {
      return Message.error("creators is null in the parameters of the request(请求参数中【creators】为空)");
    }

    List<String> creatorUserList = new ArrayList<>();
    try {
      creatorUserList =
          JsonUtils.jackson()
              .readValue(creatorsParam.toString(), new TypeReference<List<String>>() {});
    } catch (JsonProcessingException e) {
      return Message.error("parameters:creators parsing failed(请求参数【creators】解析失败)");
    }

    List<String> engineTypeList = new ArrayList<>();
    if (engineTypesParam != null && !engineTypesParam.isNull()) {
      try {
        engineTypeList =
            JsonUtils.jackson()
                .readValue(engineTypesParam.toString(), new TypeReference<List<String>>() {});
      } catch (JsonProcessingException e) {
        return Message.error("parameters:engineTypes parsing failed(请求参数【engineTypes】解析失败)");
      }
    }

    List<String> statusStrList = new ArrayList<>();
    if (statussParam != null && !statussParam.isNull()) {
      try {
        statusStrList =
            JsonUtils.jackson()
                .readValue(statussParam.toString(), new TypeReference<List<String>>() {});
      } catch (JsonProcessingException e) {
        return Message.error("parameters:statuss parsing failed(请求参数【statuss】解析失败)");
      }
    }

    String username = ModuleUserUtils.getOperationUser(req, "ecList");

    String token = ModuleUserUtils.getToken(req);
    // check special admin token
    if (StringUtils.isNotBlank(token)) {
      if (!Configuration.isAdminToken(token)) {
        logger.warn("Token:{} has no permission to query ecList.", token);
        return Message.error("Token:" + token + " has no permission to query ecList.");
      }
    } else if (!Configuration.isAdmin(username)) {
      logger.warn("User:{} has no permission to query ecList.", username);
      return Message.error("User:" + username + " has no permission to query ecList.");
    }

    for (String creatorUser : creatorUserList) {
      if (null != creatorUser && !ECResourceInfoUtils.checkNameValid(creatorUser)) {
        return Message.error("Invalid creator: " + creatorUser);
      }
    }

    logger.info(
        "request parameters creatorUserList:[{}], engineTypeList:[{}], statusStrList:[{}]",
        String.join(",", creatorUserList),
        String.join(",", engineTypeList),
        String.join(",", statusStrList));

    List<Map<String, Object>> list =
        ecResourceInfoService.getECResourceInfoList(creatorUserList, engineTypeList, statusStrList);

    return Message.ok().data("ecList", list);
  }
}
