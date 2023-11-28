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

package org.apache.linkis.monitor.until;

import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.monitor.config.MonitorConfig;
import org.apache.linkis.monitor.constants.Constants;
import org.apache.linkis.monitor.utils.alert.AlertDesc;
import org.apache.linkis.monitor.utils.alert.ims.MonitorAlertUtils;
import org.apache.linkis.monitor.utils.alert.ims.PooledImsAlertUtils;
import org.apache.linkis.monitor.utils.log.LogUtils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

import java.util.*;
import java.util.concurrent.*;

import scala.concurrent.ExecutionContextExecutorService;

import org.slf4j.Logger;

public class ThreadUtils extends ApplicationContextEvent {

  private static final Logger logger = LogUtils.stdOutLogger();

  public static ExecutionContextExecutorService executors =
      Utils.newCachedExecutionContext(5, "alert-pool-thread-", false);

  public ThreadUtils(ApplicationContext source) {
    super(source);
  }

  public static String run(List<String> cmdList, String shellName) {
    FutureTask future = new FutureTask(() -> Utils.exec(cmdList.toArray(new String[2]), -1));
    executors.submit(future);
    String msg = "";
    try {
      msg = future.get(MonitorConfig.SHELL_TIMEOUT.getValue(), TimeUnit.MINUTES).toString();
    } catch (TimeoutException e) {
      logger.info("execute shell time out {}", shellName);
      HashMap<String, String> parms = new HashMap<>();
      parms.put("$shellName", shellName);
      Map<String, AlertDesc> ecmResourceAlerts =
          MonitorAlertUtils.getAlerts(Constants.THREAD_TIME_OUT_IM(), parms);
      PooledImsAlertUtils.addAlert(ecmResourceAlerts.get("12014"));
    } catch (ExecutionException | InterruptedException e) {
      logger.error("Thread error msg {}", e.getMessage());
    }
    return msg;
  }
}
