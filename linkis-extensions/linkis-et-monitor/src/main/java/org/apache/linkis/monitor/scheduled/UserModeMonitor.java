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

package org.apache.linkis.monitor.scheduled;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.governance.common.entity.task.RequestPersistTask;
import org.apache.linkis.httpclient.dws.config.DWSClientConfig;
import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.monitor.config.MonitorConfig;
import org.apache.linkis.monitor.constants.Constants;
import org.apache.linkis.monitor.entity.ClientSingleton;
import org.apache.linkis.monitor.utils.alert.AlertDesc;
import org.apache.linkis.monitor.utils.alert.ims.MonitorAlertUtils;
import org.apache.linkis.monitor.utils.alert.ims.PooledImsAlertUtils;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.linkis.ujes.client.UJESClient;
import org.apache.linkis.ujes.client.UJESClientImpl;
import org.apache.linkis.ujes.client.request.GetTableStatisticInfoAction;
import org.apache.linkis.ujes.client.request.JobSubmitAction;
import org.apache.linkis.ujes.client.response.GetTableStatisticInfoResult;
import org.apache.linkis.ujes.client.response.JobExecuteResult;
import org.apache.linkis.ujes.client.response.JobInfoResult;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.google.gson.internal.LinkedTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * * User mode monitoring: regularly trigger scripts to monitor whether the engine status is running
 * normally
 */
@Component
public class UserModeMonitor {

  private static final Logger logger = LoggerFactory.getLogger(UserModeMonitor.class);

  private static final DWSClientConfig clientConfig =
      ClientSingleton.createClientConfig(null, null);

  private static final UJESClient client = new UJESClientImpl(clientConfig);

  @Scheduled(cron = "${linkis.monitor.user.mode.cron}")
  public void job() {
    Optional.ofNullable(MonitorConfig.USER_MODE_ENGINE.getValue())
        .ifPresent(
            configStr -> {
              ArrayList<LinkedTreeMap<String, String>> userModeStr =
                  BDPJettyServerHelper.gson().fromJson(configStr, ArrayList.class);
              userModeStr.forEach(
                  engine -> {
                    // 3. build job and execute
                    JobExecuteResult jobExecuteResult = toSubmit(engine);
                    logger.info(
                        "start run engineType: {},job id : {}",
                        engine.get("engineType"),
                        jobExecuteResult.taskID());
                    HashMap<String, String> parms = new HashMap<>();
                    parms.put("$engineType", engine.get("engineType"));
                    parms.put("$url", Configuration.GATEWAY_URL().getValue());
                    parms.put("$jobId", jobExecuteResult.taskID());
                    Utils.sleepQuietly(MonitorConfig.USER_MODE_TIMEOUT.getValue() * 1000);
                    JobInfoResult jobInfo = client.getJobInfo(jobExecuteResult);
                    if (jobInfo.isCompleted()) {
                      if (jobInfo.getJobStatus().equals("Failed")) {
                        logger.info(
                            "run fail engineType: {},job id : {}",
                            engine.get("engineType"),
                            jobExecuteResult.taskID());
                        RequestPersistTask requestPersistTask = jobInfo.getRequestPersistTask();
                        parms.put("$errorCode", String.valueOf(requestPersistTask.getErrCode()));
                        parms.put("$errorMsg", requestPersistTask.getErrDesc());
                        Map<String, AlertDesc> failedAlerts =
                            MonitorAlertUtils.getAlerts(Constants.USER_RESOURCE_MONITOR(), parms);
                        PooledImsAlertUtils.addAlert(failedAlerts.get("12012"));
                      }
                    } else {
                      logger.info(
                          "run timeout engineType: {},job id : {}",
                          engine.get("engineType"),
                          jobExecuteResult.taskID());
                      Map<String, AlertDesc> alerts =
                          MonitorAlertUtils.getAlerts(Constants.USER_RESOURCE_MONITOR(), parms);
                      PooledImsAlertUtils.addAlert(alerts.get("12011"));
                    }
                  });
            });
  }

  private static JobExecuteResult toSubmit(LinkedTreeMap<String, String> engine) {
    // 1. build  params
    // set label map :EngineTypeLabel/UserCreatorLabel/EngineRunTypeLabel/Tenant
    Map<String, Object> labels = new HashMap<String, Object>();
    labels.put(
        LabelKeyConstant.ENGINE_TYPE_KEY, engine.get("engineType")); // required engineType Label
    labels.put(
        LabelKeyConstant.USER_CREATOR_TYPE_KEY,
        engine.get("executeUser") + "-IDE"); // required execute user and creator eg:hadoop-IDE
    labels.put(LabelKeyConstant.CODE_TYPE_KEY, engine.get("runType")); // required codeType
    Map<String, Object> startupMap = new HashMap<String, Object>(16);
    // setting linkis params
    // startupMap.put("wds.linkis.rm.yarnqueue", "dws");
    // 2. build jobSubmitAction
    JobSubmitAction jobSubmitAction =
        JobSubmitAction.builder()
            .addExecuteCode(engine.get("code"))
            .setStartupParams(startupMap)
            .setUser(engine.get("executeUser")) // submit user
            .addExecuteUser(engine.get("executeUser")) // execute user
            .setLabels(labels)
            .build();
    // 3. to execute
    return client.submit(jobSubmitAction);
  }

  @Scheduled(cron = "${linkis.monitor.user.db.cron:0 0/10 * * * ?}")
  public void dbJob() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("readTimeout", MonitorConfig.USER_MODE_INTERFACE_TIMEOUT.getValue());
    DWSClientConfig clientConfig = ClientSingleton.createClientConfig(null, properties);
    UJESClientImpl ujesClient = new UJESClientImpl(clientConfig);
    GetTableStatisticInfoAction builder =
        GetTableStatisticInfoAction.builder()
            .setUser("hadoop")
            .setDatabase("default")
            .setTable("dual")
            .builder();
    HashMap<String, String> parms = new HashMap<>();
    try {
      GetTableStatisticInfoResult tableStatisticInfo = ujesClient.getTableStatisticInfo(builder);
      if (tableStatisticInfo.getStatus() != 0) {
        logger.info("元数据查询服务用户态，执行失败，异常信息：" + tableStatisticInfo.getMessage());
        //                parms.put("$msg", tableStatisticInfo.getMessage());
        //                Map<String, AlertDesc> failedAlerts =
        // MonitorAlertUtils.getAlerts(Constants.USER_RESOURCE_MONITOR(), parms);
        //                PooledImsAlertUtils.addAlert(failedAlerts.get("12017"));
      }
    } catch (Exception e) {
      if (e instanceof SocketTimeoutException) {
        Integer timeoutValue = MonitorConfig.USER_MODE_INTERFACE_TIMEOUT.getValue();
        long timeout = TimeUnit.MILLISECONDS.toSeconds(timeoutValue);
        logger.info("元数据查询服务用户态，执行超时：" + timeout + "秒");
        //                parms.put("$timeout", String.valueOf(timeout));
        //                Map<String, AlertDesc> failedAlerts =
        // MonitorAlertUtils.getAlerts(Constants.USER_RESOURCE_MONITOR(), parms);
        //                PooledImsAlertUtils.addAlert(failedAlerts.get("12018"));
      } else {
        logger.error("元数据查询服务用户态，执行异常：" + e);
        //                parms.put("$msg", e.getMessage());
        //                Map<String, AlertDesc> failedAlerts =
        // MonitorAlertUtils.getAlerts(Constants.USER_RESOURCE_MONITOR(), parms);
        //                PooledImsAlertUtils.addAlert(failedAlerts.get("12017"));
      }
    }
  }
}
