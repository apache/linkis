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
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;

import org.apache.commons.lang3.time.DateUtils;

import org.springframework.beans.factory.annotation.Autowired;
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

@Api(tags = "query api")
@RestController
@RequestMapping(path = "/jobhistory/jobstatistics")
public class StatisticsRestfulApi {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private JobStatisticsQueryService jobStatisticsQueryService;

  @ApiOperation(value = "count", notes = "count", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "startDate", dataType = "long"),
    @ApiImplicitParam(name = "endDate", required = false, dataType = "long", value = "end date"),
  })
  /** Method list should not contain subjob, which may cause performance problems. */
  @RequestMapping(path = "/count", method = RequestMethod.GET)
  public Message count(
      HttpServletRequest req,
      @RequestParam(value = "startDate", required = false) Long startDate,
      @RequestParam(value = "endDate", required = false) Long endDate)
      throws QueryException {
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
    JobStatistics jobStatistics = jobStatisticsQueryService.taskExecutionStatistics(sDate, eDate);
    JobStatistics engineStatistics =
        jobStatisticsQueryService.engineExecutionStatistics(sDate, eDate);

    return Message.ok()
        .data("sumCount", jobStatistics.getSumCount())
        .data("succeedCount", jobStatistics.getSucceedCount())
        .data("failedCount", jobStatistics.getFailedCount())
        .data("cancelledCount", jobStatistics.getCancelledCount())
        .data("countEngine", engineStatistics.getSumCount())
        .data("countEngineSucceed", engineStatistics.getSucceedCount())
        .data("countEngineFailed", engineStatistics.getFailedCount())
        .data("countEngineShutting", engineStatistics.getCancelledCount());
  }
}
