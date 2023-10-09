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

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.monitor.config.MonitorConfig;
import org.apache.linkis.monitor.constants.Constants;
import org.apache.linkis.monitor.entity.IndexEntity;
import org.apache.linkis.monitor.until.HttpsUntils;
import org.apache.linkis.monitor.utils.alert.AlertDesc;
import org.apache.linkis.monitor.utils.alert.ims.MonitorAlertUtils;
import org.apache.linkis.monitor.utils.alert.ims.PooledImsAlertUtils;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.server.BDPJettyServerHelper;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import com.google.gson.internal.LinkedTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * * Entrance monitors the number of tasks for specified users and systems. If the configured
 * threshold is exceeded, an alarm will be triggered.
 */
@Component
@PropertySource(value = "classpath:linkis-et-monitor.properties", encoding = "UTF-8")
public class EntranceTaskMonitor {

  private static final Logger logger = LoggerFactory.getLogger(EntranceTaskMonitor.class);

  private static final String ENTRANCE_RUNNING_TASK = "entrance_running_task";
  private static final String ENTRANCE_QUEUED_TASK = "entrance_queued_task";

  @Scheduled(cron = "${linkis.monitor.entrance.task.cron}")
  public void entranceTask() throws IOException {
    List<LinkedTreeMap<String, String>> userlist = new ArrayList<>();
    String value = MonitorConfig.ENTRANCE_TASK_USERLIST.getValue();
    if (StringUtils.isNotBlank(value)) {
      userlist = BDPJettyServerHelper.gson().fromJson(value, ArrayList.class);
    }

    userlist.forEach(
        entranceEntity -> {
          Map<String, Object> data = new HashMap<>();
          try {
            data =
                MapUtils.getMap(
                    HttpsUntils.getEntranceTask(null, entranceEntity.get("username"), null),
                    "data");
            logger.info("TaskMonitor userlist response  {}:", data);
          } catch (IOException e) {
            logger.warn("failed to get EntranceTask data");
          }

          int runningNumber =
              null != entranceEntity.get("runningtasks")
                  ? Integer.parseInt(entranceEntity.get("runningtasks"))
                  : 0;
          int queuedNumber =
              null != entranceEntity.get("queuedtasks")
                  ? Integer.parseInt(entranceEntity.get("queuedtasks"))
                  : 0;

          BigDecimal runningtotal = new BigDecimal((int) data.get("runningNumber"));
          BigDecimal queuedtotal = new BigDecimal((int) data.get("queuedNumber"));
          BigDecimal total = runningtotal.add(queuedtotal);
          HashMap<String, String> parms = new HashMap<>();
          parms.put("$username", entranceEntity.get("username"));
          parms.put("$alteruser", entranceEntity.get("alteruser"));
          parms.put("$url", Configuration.GATEWAY_URL().getValue());
          // 获取标准阈值
          if (runningtotal.intValue() > runningNumber) {
            // 触发告警 用户运行任务满
            parms.put("$runningtask", String.valueOf(runningNumber));
            Map<String, AlertDesc> ecmResourceAlerts =
                MonitorAlertUtils.getAlerts(Constants.ALERT_RESOURCE_MONITOR(), parms);
            PooledImsAlertUtils.addAlert(ecmResourceAlerts.get("12005"));
          }
          if (queuedtotal.intValue() > queuedNumber) {
            // 触发告警 用户排队任务满
            parms.put("$queuedtask", String.valueOf(queuedNumber));
            Map<String, AlertDesc> ecmResourceAlerts =
                MonitorAlertUtils.getAlerts(Constants.ALERT_RESOURCE_MONITOR(), parms);
            PooledImsAlertUtils.addAlert(ecmResourceAlerts.get("12006"));
          }
          int usertotalTask = MonitorConfig.ENTRANCE_TASK_USERTOTAL.getValue();
          if (total.intValue() > usertotalTask) {
            parms.put("$tasktotal", String.valueOf(usertotalTask));
            Map<String, AlertDesc> ecmResourceAlerts =
                MonitorAlertUtils.getAlerts(Constants.ALERT_RESOURCE_MONITOR(), parms);
            PooledImsAlertUtils.addAlert(ecmResourceAlerts.get("12007"));
          }
        });
    Map<String, Object> likisData = null;
    try {
      likisData = MapUtils.getMap(HttpsUntils.getEntranceTask(null, "hadoop", null), "data");
      logger.info("TaskMonitor hadoop response  {}:", likisData);
    } catch (IOException e) {
      logger.warn("failed to get EntranceTask data");
    }
    // 系统监控
    BigDecimal runningNumber = new BigDecimal((int) likisData.get("runningNumber"));
    BigDecimal queuedNumber = new BigDecimal((int) likisData.get("queuedNumber"));
    BigDecimal total = runningNumber.add(queuedNumber);

    HashMap<String, String> parms = new HashMap<>();
    parms.put("$url", Configuration.GATEWAY_URL().getValue());
    int linkisTotalMajor = MonitorConfig.ENTRANCE_TASK_TOTAL_MAJOR.getValue();
    int linkisTotalMinor = MonitorConfig.ENTRANCE_TASK_TOTAL_MINOR.getValue();
    if (total.intValue() >= linkisTotalMajor) {
      // 触发告警Major
      parms.put("$taskmajor", String.valueOf(linkisTotalMajor));
      logger.info("TaskMonitor parms {}:", parms);
      Map<String, AlertDesc> ecmResourceAlerts =
          MonitorAlertUtils.getAlerts(Constants.ALERT_RESOURCE_MONITOR(), parms);
      PooledImsAlertUtils.addAlert(ecmResourceAlerts.get("12009"));

    } else if (total.intValue() >= linkisTotalMinor) {
      parms.put("$taskminor", String.valueOf(linkisTotalMinor));
      logger.info("TaskMonitor parms {}:", parms);
      Map<String, AlertDesc> ecmResourceAlerts =
          MonitorAlertUtils.getAlerts(Constants.ALERT_RESOURCE_MONITOR(), parms);
      PooledImsAlertUtils.addAlert(ecmResourceAlerts.get("12008"));
    }
    resourceSendToIms();
  }

  public static void resourceSendToIms() {
    ServiceInstance[] instances =
        Sender.getInstances(GovernanceCommonConf.ENTRANCE_SERVICE_NAME().getValue());
    if (null != instances) {
      for (ServiceInstance instance : instances) {
        String serviceInstance = instance.getInstance();
        try {
          Map<String, Object> instanceData =
              MapUtils.getMap(HttpsUntils.getEntranceTask(null, "hadoop", serviceInstance), "data");
          int runningNumber = 0;
          int queuedNumber = 0;
          if (instanceData.containsKey("runningNumber")) {
            runningNumber = (int) instanceData.get("runningNumber");
          }
          if (instanceData.containsKey("queuedNumber")) {
            queuedNumber = (int) instanceData.get("queuedNumber");
          }
          logger.info("ResourceMonitor send index ");
          List<IndexEntity> list = new ArrayList<>();
          list.add(
              new IndexEntity(
                  serviceInstance,
                  "entrance",
                  ENTRANCE_RUNNING_TASK,
                  HttpsUntils.localHost,
                  String.valueOf(runningNumber)));
          list.add(
              new IndexEntity(
                  serviceInstance,
                  "entrance",
                  ENTRANCE_QUEUED_TASK,
                  HttpsUntils.localHost,
                  String.valueOf(queuedNumber)));
          HttpsUntils.sendIndex(list);
        } catch (IOException e) {
          logger.warn("failed to send EcmResource index :" + e);
        }
      }
    }
  }
}
