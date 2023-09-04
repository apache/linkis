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

package org.apache.linkis.monitor.scan.app.monitor.config;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.Configuration;

public class MonitorConfig {

  public static final String shellPath = Configuration.getLinkisHome() + "/admin/";

  public static final CommonVars<String> GATEWAY_URL = CommonVars.apply("wds.linkis.gateway.url");

  public static final CommonVars<Integer> USER_MODE_TIMEOUT =
      CommonVars.apply("linkis.monitor.user.timeOut", 300);
  public static final CommonVars<String> USER_MODE_AUTHTOKEN =
      CommonVars.apply("linkis.monitor.user.authToken","VALIDATOR-AUTH");
  public static final CommonVars<String> USER_MODE_ENGINE =
      CommonVars.apply("linkis.monitor.user.enginelist","[]");

  public static final CommonVars<Double> ECM_TASK_MAJOR =
      CommonVars.apply("linkis.monitor.ecmResourceTask.major", 0.03);
  public static final CommonVars<Double> ECM_TASK_MINOR =
      CommonVars.apply("linkis.monitor.ecmResourceTask.minor", 0.1);
  public static final CommonVars<String> ECM_TASK_IMURL =
      CommonVars.apply("linkis.monitor.metrics.imsUrl");
  public static final CommonVars<String> ECM_TASK_USER_AUTHKEY =
      CommonVars.apply("linkis.monitor.metrics.userAuthKey");

  public static final CommonVars<Long> JOB_HISTORY_TIME_EXCEED =
      CommonVars.apply("linkis.monitor.jobhistory.id.timeExceed",0L);

  public static final CommonVars<Integer> ENTRANCE_TASK_USERTOTAL =
      CommonVars.apply("linkis.monitor.entranceTask.userTotalTask", 1000);
  public static final CommonVars<Integer> ENTRANCE_TASK_TOTAL_MAJOR =
      CommonVars.apply("linkis.monitor.entranceTask.linkisTotalTaskMajor", 50000);
  public static final CommonVars<Integer> ENTRANCE_TASK_TOTAL_MINOR =
      CommonVars.apply("linkis.monitor.entranceTask.linkisTotalTaskMinor", 10000);
  public static final CommonVars<String> ENTRANCE_TASK_USERLIST =
      CommonVars.apply("linkis.monitor.entranceTask.userlist","[]");

  public static final CommonVars<Integer> SCHEDULED_CONFIG_NUM =
      CommonVars.apply("linkis.monitor.scheduled.pool.cores.num", 10);

  public static final CommonVars<Integer> SHELL_TIMEOUT =
      CommonVars.apply("linkis.monitor.shell.time.out.minute", 30);

  public static final CommonVars<Integer> USER_MODE_INTERFACE_TIMEOUT =
          CommonVars.apply("linkis.monitor.user.mode.time.out", 30*1000);

  public static final CommonVars<String> CHATBOT_KEY_ID = CommonVars.apply("linkis.monitor.chatbot.key.id","23e6afad1b78a0c5eed67e4d24de7063");
  public static final CommonVars<String> CHATBOT_TYPE = CommonVars.apply("linkis.monitor.chatbot.type","text");
  public static final CommonVars<String> CHATBOT_SERVICE_NAME= CommonVars.apply("linkis.monitor.chatbot.serviceName","大数据生产助手(BDP_PRD)");
  public static final CommonVars<String> CHATBOT_URL= CommonVars.apply("linkis.monitor.chatbot.url","http://172.21.3.43:1377/pros-chatbot/yuanfang/sendEMsg");
  public static final CommonVars<String> SOLUTION_URL = CommonVars.apply("linkis.monitor.jobhistory.solution.url", "http://kn.dss.weoa.com/linkis/qa");
}
