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

package org.apache.linkis.jobhistory.restful.api;

import org.apache.linkis.jobhistory.conversions.TaskConversions;
import org.apache.linkis.jobhistory.entity.JobHistory;
import org.apache.linkis.jobhistory.entity.MonitorVO;
import org.apache.linkis.jobhistory.service.JobHistoryQueryService;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "query api")
@RestController
@RequestMapping(path = "/jobhistory/setting")
public class JobhistorySettingApi {

  @Autowired private JobHistoryQueryService jobHistoryQueryService;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  /** Method list should not contain subjob, which may cause performance problems. */
  @ApiOperation(value = "addObserveInfo", notes = "add Observe Info", response = Message.class)
  @RequestMapping(path = "/addObserveInfo", method = RequestMethod.POST)
  public Message addObserveInfo(HttpServletRequest req, @RequestBody MonitorVO monitor) {
    String username = ModuleUserUtils.getOperationUser(req, "addObserveInfo");
    // Parameter verification
    if (null == monitor.getTaskId()) {
      return Message.error("TaskId can't be empty ");
    }
    if (StringUtils.isBlank(monitor.getReceiver())) {
      return Message.error("Receiver can't be empty");
    }
    if (null == monitor.getExtra()) {
      return Message.error("Extra can't be empty ");
    } else {
      Map<String, String> extra = monitor.getExtra();
      if (StringUtils.isBlank(extra.getOrDefault("title", ""))) {
        return Message.error("title can't be empty ");
      }
      if (StringUtils.isBlank(extra.getOrDefault("detail", ""))) {
        return Message.error("detail can't be empty ");
      }
    }
    if (StringUtils.isBlank(monitor.getMonitorLevel())) {
      return Message.error("MonitorLevel can't be empty ");
    }
    if (StringUtils.isBlank(monitor.getSubSystemId()))
      return Message.error("SubSystemId can't be empty ");
    // Get jobInfo according to ID
    JobHistory jobHistory =
        jobHistoryQueryService.getJobHistoryByIdAndName(monitor.getTaskId(), null);
    if (!username.equals(jobHistory.getSubmitUser())) {
      return Message.error("Only submitUser can change");
    }

    if (!TaskConversions.isJobFinished(jobHistory.getStatus())) {
      // Task not completed, update job record
      String observeInfoJson = BDPJettyServerHelper.gson().toJson(monitor);
      jobHistory.setObserveInfo(observeInfoJson);
      jobHistoryQueryService.changeObserveInfoById(jobHistory);
    } else {
      return Message.error("The task has been completed, and the alarm cannot be set");
    }
    //    }
    return Message.ok();
  }

  @ApiOperation(
      value = "deleteObserveInfo",
      notes = "delete Observe Info",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "taskId", required = false, dataType = "long", value = "taskId")
  })
  @RequestMapping(path = "/deleteObserveInfo", method = RequestMethod.GET)
  public Message deleteObserveInfo(HttpServletRequest req, Long taskId) {
    String username = ModuleUserUtils.getOperationUser(req, "deleteObserveInfo");
    // Get jobInfo according to ID
    JobHistory jobHistory = jobHistoryQueryService.getJobHistoryByIdAndName(taskId, null);
    if (!username.equals(jobHistory.getSubmitUser())) {
      return Message.error("Only submitUser can change");
    }
    if (!TaskConversions.isJobFinished(jobHistory.getStatus())) {
      jobHistory.setObserveInfo("");
      jobHistoryQueryService.changeObserveInfoById(jobHistory);
    } else {
      // The alarm task has been completed
      return Message.error("The task has been completed, and the alarm cannot be set");
    }
    return Message.ok();
  }
}
