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

import org.apache.linkis.common.utils.ByteTimeUtils;
import org.apache.linkis.monitor.config.MonitorConfig;
import org.apache.linkis.monitor.constants.Constants;
import org.apache.linkis.monitor.entity.IndexEntity;
import org.apache.linkis.monitor.until.HttpsUntils;
import org.apache.linkis.monitor.utils.alert.AlertDesc;
import org.apache.linkis.monitor.utils.alert.ims.MonitorAlertUtils;
import org.apache.linkis.monitor.utils.alert.ims.PooledImsAlertUtils;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** * Monitor the usage of ECM resources for monitoring and metrics reporting */
@Component
@PropertySource(value = "classpath:linkis-et-monitor.properties", encoding = "UTF-8")
public class ResourceMonitor {

  private static final Logger logger = LoggerFactory.getLogger(ResourceMonitor.class);

  @Scheduled(cron = "${linkis.monitor.ecm.resource.cron}")
  public void ecmResourceTask() {
    Map<String, Object> resultmap = null;
    AtomicReference<String> tenant = new AtomicReference<>("租户标签：公共资源");
    AtomicReference<Double> totalMemory = new AtomicReference<>(0.0);
    AtomicReference<Double> totalInstance = new AtomicReference<>(0.0);
    AtomicReference<Double> totalCores = new AtomicReference<>(0.0);
    try {
      resultmap = HttpsUntils.sendHttp(null, null);
      logger.info("ResourceMonitor  response  {}:", resultmap);
    } catch (IOException e) {
      logger.warn("failed to get EcmResource data");
    }
    // got interface data
    Map<String, List<Map<String, Object>>> data = MapUtils.getMap(resultmap, "data");
    List<Map<String, Object>> emNodeVoList = data.getOrDefault("EMs", new ArrayList<>());
    StringJoiner minor = new StringJoiner(",");
    StringJoiner major = new StringJoiner(",");
    // deal ecm resource
    emNodeVoList.forEach(
        emNodeVo -> {
          Map<String, Object> leftResource = MapUtils.getMap(emNodeVo, "leftResource");
          Map<String, Object> maxResource = MapUtils.getMap(emNodeVo, "maxResource");
          // 新增 ECM资源告警，需补充此ECM所属租户
          List<Map<String, Object>> labels = (List<Map<String, Object>>) emNodeVo.get("labels");
          labels.stream()
              .filter(labelmap -> labelmap.containsKey("tenant"))
              .forEach(map -> tenant.set("租户标签：" + map.get("stringValue").toString()));
          String leftmemory =
              ByteTimeUtils.bytesToString((long) leftResource.getOrDefault("memory", 0));
          String maxmemory =
              ByteTimeUtils.bytesToString((long) maxResource.getOrDefault("memory", 0));

          String leftmemoryStr = leftmemory.split(" ")[0];
          String maxmemoryStr = maxmemory.split(" ")[0];

          BigDecimal leftMemory = new BigDecimal(leftmemoryStr);
          BigDecimal leftCores = new BigDecimal((int) leftResource.get("cores"));
          BigDecimal leftInstance = new BigDecimal((int) leftResource.get("instance"));
          totalMemory.set(totalMemory.get() + leftMemory.doubleValue());
          totalInstance.set(totalInstance.get() + leftInstance.doubleValue());
          totalCores.set(totalCores.get() + leftCores.doubleValue());

          BigDecimal maxMemory = new BigDecimal(maxmemoryStr);
          BigDecimal maxCores = new BigDecimal((int) maxResource.get("cores"));
          BigDecimal maxInstance = new BigDecimal((int) maxResource.get("instance"));
          double memorydouble =
              leftMemory.divide(maxMemory, 2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
          double coresdouble =
              leftCores.divide(maxCores, 2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
          double instancedouble =
              leftInstance.divide(maxInstance, 2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
          Double majorValue = MonitorConfig.ECM_TASK_MAJOR.getValue();
          Double minorValue = MonitorConfig.ECM_TASK_MINOR.getValue();
          if (((memorydouble) <= majorValue)
              || ((coresdouble) <= majorValue)
              || ((instancedouble) <= majorValue)) {
            major.add(emNodeVo.get("instance").toString());
          } else if (((memorydouble) < minorValue)
              || ((coresdouble) < minorValue)
              || ((instancedouble) < minorValue)) {
            minor.add(emNodeVo.get("instance").toString());
          }
          HashMap<String, String> replaceParm = new HashMap<>();
          replaceParm.put("$tenant", tenant.get());
          if (StringUtils.isNotBlank(major.toString())) {
            replaceParm.put("$instance", major.toString());
            replaceParm.put("$ratio", majorValue.toString());
            Map<String, AlertDesc> ecmResourceAlerts =
                MonitorAlertUtils.getAlerts(Constants.ALERT_RESOURCE_MONITOR(), replaceParm);
            PooledImsAlertUtils.addAlert(ecmResourceAlerts.get("12004"));
          }
          if (StringUtils.isNotBlank(minor.toString())) {
            replaceParm.put("$instance", minor.toString());
            replaceParm.put("$ratio", minorValue.toString());
            Map<String, AlertDesc> ecmResourceAlerts =
                MonitorAlertUtils.getAlerts(Constants.ALERT_RESOURCE_MONITOR(), replaceParm);
            PooledImsAlertUtils.addAlert(ecmResourceAlerts.get("12003"));
          }
          resourceSendToIms(
              coresdouble, memorydouble, instancedouble, HttpsUntils.localHost, "USED");
        });
    resourceSendToIms(
        totalCores.get(), totalMemory.get(), totalInstance.get(), HttpsUntils.localHost, "TOTAL");
  }

  private void resourceSendToIms(
      Double coresdouble,
      Double memorydouble,
      Double instancedouble,
      String loaclhost,
      String name) {
    List<IndexEntity> list = new ArrayList<>();
    logger.info("ResourceMonitor  send  index ");
    String core = "ECM_CPU_";
    String memory = "ECM_MEMORY_";
    String instance = "ECM_INSTANCE_";
    list.add(
        new IndexEntity(core.concat(name), "CPU", "INDEX", loaclhost, String.valueOf(coresdouble)));
    list.add(
        new IndexEntity(
            memory.concat(name), "MEMORY", "INDEX", loaclhost, String.valueOf(memorydouble)));
    list.add(
        new IndexEntity(
            instance.concat(name), "INSTANCE", "INDEX", loaclhost, String.valueOf(instancedouble)));
    try {
      HttpsUntils.sendIndex(list);
    } catch (IOException e) {
      logger.warn("failed to send EcmResource index");
    }
  }
}
