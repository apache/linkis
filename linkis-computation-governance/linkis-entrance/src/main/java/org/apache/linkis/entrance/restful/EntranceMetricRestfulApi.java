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

package org.apache.linkis.entrance.restful;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.entrance.EntranceServer;
import org.apache.linkis.entrance.execute.EntranceJob;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.utils.LabelUtil;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "entrance metrice manager")
@RestController
@RequestMapping(path = "/entrance/operation/metrics")
public class EntranceMetricRestfulApi {

  private EntranceServer entranceServer;

  private static final Logger logger = LoggerFactory.getLogger(EntranceMetricRestfulApi.class);

  @Autowired
  public void setEntranceServer(EntranceServer entranceServer) {
    this.entranceServer = entranceServer;
  }

  @ApiOperation(value = "taskinfo", notes = "get task info", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "user", dataType = "String", value = "User"),
    @ApiImplicitParam(name = "creator", dataType = "String", value = "Creator"),
    @ApiImplicitParam(name = "engineTypeLabel", dataType = "String", value = "engine type lable")
  })
  @RequestMapping(path = "/taskinfo", method = RequestMethod.GET)
  public Message taskinfo(
      HttpServletRequest req,
      @RequestParam(value = "user", required = false) String user,
      @RequestParam(value = "creator", required = false) String creator,
      @RequestParam(value = "engineTypeLabel", required = false) String engineTypeLabelValue) {
    String userName = ModuleUserUtils.getOperationUser(req, "taskinfo");
    String queryUser = user;
    if (Configuration.isNotAdmin(userName)) {
      if (StringUtils.isBlank(queryUser)) {
        queryUser = userName;
      } else if (!userName.equalsIgnoreCase(queryUser)) {
        return Message.error("Non-administrators cannot view other users' task information");
      }
    }
    String filterWords = creator;
    if (StringUtils.isNotBlank(filterWords) && StringUtils.isNotBlank(queryUser)) {
      filterWords = filterWords + "_" + queryUser;
    } else if (StringUtils.isBlank(creator)) {
      filterWords = queryUser;
    }
    EntranceJob[] undoneTasks = entranceServer.getAllUndoneTask(filterWords);
    int taskNumber = 0;
    int runningNumber = 0;
    int queuedNumber = 0;

    if (null != undoneTasks) {
      for (EntranceJob task : undoneTasks) {
        if (StringUtils.isNotBlank(engineTypeLabelValue)) {
          EngineTypeLabel engineTypeLabel =
              LabelUtil.getEngineTypeLabel(task.getJobRequest().getLabels());
          // Task types do not match, do not count
          if (null == engineTypeLabel
              || !engineTypeLabelValue.equalsIgnoreCase(engineTypeLabel.getStringValue())) {
            continue;
          }
        }
        taskNumber++;
        if (task.isRunning()) {
          runningNumber++;
        } else {
          queuedNumber++;
        }
      }
    }
    return Message.ok("success")
        .data("taskNumber", taskNumber)
        .data("runningNumber", runningNumber)
        .data("queuedNumber", queuedNumber);
  }

  @ApiOperation(value = "Status", notes = "get running task number ", response = Message.class)
  @RequestMapping(path = "/runningtask", method = RequestMethod.GET)
  public Message status(HttpServletRequest req) {

    EntranceJob[] undoneTasks = entranceServer.getAllUndoneTask("");
    Boolean isCompleted = false;
    if (null == undoneTasks || undoneTasks.length < 1) {
      isCompleted = true;
    }
    int runningTaskNumber = 0;
    if (undoneTasks != null) {
      runningTaskNumber = undoneTasks.length;
    }
    return Message.ok("success")
        .data("runningTaskNumber", runningTaskNumber)
        .data("isCompleted", isCompleted);
  }
}
