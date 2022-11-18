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

import org.apache.linkis.jobhistory.conf.JobhistoryConfiguration;
import org.apache.linkis.jobhistory.entity.JobHistory;
import org.apache.linkis.jobhistory.entity.MonitorVO;
import org.apache.linkis.jobhistory.service.JobHistoryQueryService;
import org.apache.linkis.protocol.utils.TaskUtils;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "query api")
@RestController
@RequestMapping(path = "/jobhistory/setting")
public class JobhistorySettingApi {

  @Autowired private JobHistoryQueryService jobHistoryQueryService;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @RequestMapping(path = "/addObserveInfo", method = RequestMethod.POST)
  public Message addObserveInfo(HttpServletRequest req, @RequestBody MonitorVO monitor) {
    String username = ModuleUserUtils.getOperationUser(req, "isNotice");
    if (null == monitor.getTaskId()) {
      return Message.error("TaskId can't be empty ");
    }
    if (CollectionUtils.isEmpty(monitor.getMonitorStatus())) {
      return Message.error("MonitorStatus can't be empty ");
    }
    if (StringUtils.isBlank(monitor.getReceiver())) {
      return Message.error("Receiver can't be empty");
    }
    if (null == monitor.getExtra()) {
      return Message.error("Extra can't be empty ");
    } else {
      Map<String, String> extra = monitor.getExtra();
      if (StringUtils.isBlank(extra.getOrDefault("title", "")))
        return Message.error("title can't be empty ");
      if (StringUtils.isBlank(extra.getOrDefault("detail", "")))
        return Message.error("detail can't be empty ");
    }
    if (StringUtils.isBlank(monitor.getMonitorLevel())) {
      return Message.error("MonitorLevel can't be empty ");
    }
    // 根据id获取jobInfo
    JobHistory jobHistory =
        jobHistoryQueryService.getJobHistoryByIdAndName(monitor.getTaskId(), null);
    // 根据用户自定义 在runtimeMap里面设置是否告警（task.notification.conditions）
    Map<String, Object> map =
        BDPJettyServerHelper.gson().fromJson(jobHistory.getParams(), new HashMap<>().getClass());
    Map<String, Object> runtimeMap = TaskUtils.getRuntimeMap(map);
    if (runtimeMap.containsKey("task.notification.conditions")) {
      // 判断任务是否已经完成，完成则不允许修改
      String[] strings = JobhistoryConfiguration.DIRTY_DATA_UNFINISHED_JOB_STATUS();
      boolean result =
          Arrays.stream(strings).anyMatch(S -> S.equals(jobHistory.getStatus().toUpperCase()));
      if (result) {
        // 任务未完成 ，更新job记录
        String observeInfoJson = BDPJettyServerHelper.gson().toJson(monitor);
        jobHistory.setObserveInfo(observeInfoJson);
        jobHistoryQueryService.changeObserveInfoById(jobHistory);
      } else {
        // 告警任务已经完成
        return Message.error("The task has been completed, and the alarm cannot be set");
      }
    }
    return Message.ok();
  }
}
