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

import org.apache.linkis.governance.common.entity.job.QueryException;
import org.apache.linkis.jobhistory.entity.JobStatistics;
import org.apache.linkis.jobhistory.service.JobStatisticsQueryService;
import org.apache.linkis.jobhistory.util.QueryUtils;
import org.apache.linkis.server.Message;

import org.apache.commons.lang3.time.DateUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "jobstatistics api")
@RestController
@RequestMapping(path = "/jobhistory/jobstatistics")
public class StatisticsRestfulApi {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private JobStatisticsQueryService jobStatisticsQueryService;

  @ApiOperation(value = "taskCount", notes = "taskCount", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "startDate", dataType = "long"),
    @ApiImplicitParam(name = "endDate", required = false, dataType = "long", value = "end date"),
    @ApiImplicitParam(name = "executeApplicationName", dataType = "String"),
    @ApiImplicitParam(name = "creator", required = false, dataType = "String", value = "creator"),
    @ApiImplicitParam(
        name = "proxyUser",
        required = false,
        dataType = "String",
        value = "proxyUser"),
  })
  @RequestMapping(path = "/taskCount", method = RequestMethod.GET)
  public Message taskCount(
      HttpServletRequest req,
      @RequestParam(value = "startDate", required = false) Long startDate,
      @RequestParam(value = "endDate", required = false) Long endDate,
      @RequestParam(value = "executeApplicationName", required = false)
          String executeApplicationName,
      @RequestParam(value = "creator", required = false) String creator,
      @RequestParam(value = "proxyUser", required = false) String proxyUser)
      throws IOException, QueryException {
    if (endDate == null) {
      endDate = System.currentTimeMillis();
    }
    if (startDate == null) {
      startDate = 0L;
    }
    Date sDate = new Date(startDate);
    Date eDate = new Date(endDate);
    if (startDate == 0L) {
      sDate = DateUtils.addDays(eDate, -1);
    }
    if (sDate.getTime() == eDate.getTime()) {
      Calendar instance = Calendar.getInstance();
      instance.setTimeInMillis(endDate);
      instance.add(Calendar.DAY_OF_MONTH, 1);
      eDate = new Date(instance.getTime().getTime());
    }
    if (StringUtils.isEmpty(proxyUser)) {
      proxyUser = null;
    } else {
      if (!QueryUtils.checkNameValid(proxyUser)) {
        return Message.error("Invalid proxyUser : " + proxyUser);
      }
    }
    if (StringUtils.isEmpty(creator)) {
      creator = null;
    } else {
      if (!QueryUtils.checkNameValid(creator)) {
        return Message.error("Invalid creator : " + creator);
      }
    }
    if (!StringUtils.isEmpty(executeApplicationName)) {
      if (!QueryUtils.checkNameValid(executeApplicationName)) {
        return Message.error("Invalid applicationName : " + executeApplicationName);
      }
    } else {
      executeApplicationName = null;
    }
    JobStatistics jobStatistics =
        jobStatisticsQueryService.taskExecutionStatistics(
            sDate, eDate, proxyUser, creator, executeApplicationName);

    return Message.ok()
        .data("sumCount", jobStatistics.getSumCount())
        .data("succeedCount", jobStatistics.getSucceedCount())
        .data("failedCount", jobStatistics.getFailedCount())
        .data("cancelledCount", jobStatistics.getCancelledCount());
  }

  @ApiOperation(value = "engineCount", notes = "engineCount", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "startDate", dataType = "long"),
    @ApiImplicitParam(name = "endDate", required = false, dataType = "long", value = "end date"),
    @ApiImplicitParam(name = "executeApplicationName", dataType = "String"),
    @ApiImplicitParam(name = "creator", required = false, dataType = "String", value = "creator"),
    @ApiImplicitParam(
        name = "proxyUser",
        required = false,
        dataType = "String",
        value = "proxyUser"),
  })
  @RequestMapping(path = "/engineCount", method = RequestMethod.GET)
  public Message engineCount(
      HttpServletRequest req,
      @RequestParam(value = "startDate", required = false) Long startDate,
      @RequestParam(value = "endDate", required = false) Long endDate,
      @RequestParam(value = "executeApplicationName", required = false)
          String executeApplicationName,
      @RequestParam(value = "creator", required = false) String creator,
      @RequestParam(value = "proxyUser", required = false) String proxyUser)
      throws IOException, QueryException {
    if (endDate == null) {
      endDate = System.currentTimeMillis();
    }
    if (startDate == null) {
      startDate = 0L;
    }
    Date sDate = new Date(startDate);
    Date eDate = new Date(endDate);
    if (startDate == 0L) {
      sDate = DateUtils.addDays(eDate, -1);
    }
    if (sDate.getTime() == eDate.getTime()) {
      Calendar instance = Calendar.getInstance();
      instance.setTimeInMillis(endDate);
      instance.add(Calendar.DAY_OF_MONTH, 1);
      eDate = new Date(instance.getTime().getTime());
    }
    if (StringUtils.isEmpty(proxyUser)) {
      proxyUser = null;
    } else {
      if (!QueryUtils.checkNameValid(proxyUser)) {
        return Message.error("Invalid proxyUser : " + proxyUser);
      }
    }
    if (StringUtils.isEmpty(creator)) {
      creator = null;
    } else {
      if (!QueryUtils.checkNameValid(creator)) {
        return Message.error("Invalid creator : " + creator);
      }
    }
    if (!StringUtils.isEmpty(executeApplicationName)) {
      if (!QueryUtils.checkNameValid(executeApplicationName)) {
        return Message.error("Invalid applicationName : " + executeApplicationName);
      }
    } else {
      executeApplicationName = null;
    }
    JobStatistics jobStatistics =
        jobStatisticsQueryService.engineExecutionStatistics(
            sDate, eDate, proxyUser, creator, executeApplicationName);

    return Message.ok()
        .data("countEngine", jobStatistics.getSumCount())
        .data("countEngineSucceed", jobStatistics.getSucceedCount())
        .data("countEngineFailed", jobStatistics.getFailedCount())
        .data("countEngineShutting", jobStatistics.getCancelledCount());
  }
}
