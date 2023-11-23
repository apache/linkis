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

package org.apache.linkis.monitor.config;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.Configuration;

public class MonitorConfig {

  public static final String shellPath = Configuration.getLinkisHome() + "/admin/";

  public static final CommonVars<Integer> USER_MODE_TIMEOUT =
      CommonVars.apply("linkis.monitor.user.timeOut", 300);
  public static final CommonVars<String> USER_MODE_ENGINE =
      CommonVars.apply("linkis.monitor.user.enginelist", "[]");

  public static final CommonVars<Double> ECM_TASK_MAJOR =
      CommonVars.apply("linkis.monitor.ecmResourceTask.major", 0.03);
  public static final CommonVars<Double> ECM_TASK_MINOR =
      CommonVars.apply("linkis.monitor.ecmResourceTask.minor", 0.1);
  public static final CommonVars<String> ECM_TASK_IMURL =
      CommonVars.apply("linkis.monitor.metrics.imsUrl");
  public static final CommonVars<String> ECM_TASK_USER_AUTHKEY =
      CommonVars.apply("linkis.monitor.metrics.userAuthKey");

  public static final CommonVars<Long> JOB_HISTORY_TIME_EXCEED =
      CommonVars.apply("linkis.monitor.jobhistory.id.timeExceed", 0L);

  public static final CommonVars<Integer> ENTRANCE_TASK_USERTOTAL =
      CommonVars.apply("linkis.monitor.entranceTask.userTotalTask", 1000);
  public static final CommonVars<Integer> ENTRANCE_TASK_TOTAL_MAJOR =
      CommonVars.apply("linkis.monitor.entranceTask.linkisTotalTaskMajor", 50000);
  public static final CommonVars<Integer> ENTRANCE_TASK_TOTAL_MINOR =
      CommonVars.apply("linkis.monitor.entranceTask.linkisTotalTaskMinor", 10000);
  public static final CommonVars<String> ENTRANCE_TASK_USERLIST =
      CommonVars.apply("linkis.monitor.entranceTask.userlist", "[]");

  public static final CommonVars<Integer> SCHEDULED_CONFIG_NUM =
      CommonVars.apply("linkis.monitor.scheduled.pool.cores.num", 10);

  public static final CommonVars<Integer> SHELL_TIMEOUT =
      CommonVars.apply("linkis.monitor.shell.time.out.minute", 30);

  public static final CommonVars<Integer> USER_MODE_INTERFACE_TIMEOUT =
      CommonVars.apply("linkis.monitor.user.mode.time.out", 30 * 1000);

  public static final CommonVars<String> SOLUTION_URL =
      CommonVars.apply(
          "linkis.monitor.jobhistory.solution.url",
          "https://linkis.apache.org/docs/latest/tuning-and-troubleshooting/error-guide/error-code");

  public static final CommonVars<String> TASK_RUNTIME_TIMEOUT_DESC =
      CommonVars.apply(
          "linkis.monitor.jobhistory.task.timeout.desc",
          "[Linkis任务信息]您好，您在Linkis/DSS提交的任务(任务ID:{0})，已经运行超过{1}h，"
              + "请关注是否任务正常，如果不正常您可以到Linkis/DSS管理台进行任务的kill，集群信息为BDAP({2})。详细解决方案见Q47：{3} ");
}
