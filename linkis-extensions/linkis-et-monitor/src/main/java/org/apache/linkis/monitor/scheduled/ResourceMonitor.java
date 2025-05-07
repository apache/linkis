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
import java.math.RoundingMode;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** * Monitor the usage of ECM resources for monitoring and metrics reporting */
@Component
@PropertySource(value = "classpath:linkis-et-monitor.properties", encoding = "UTF-8")
public class ResourceMonitor {

  private static final Logger logger = LoggerFactory.getLogger(ResourceMonitor.class);

  @Scheduled(cron = "${linkis.monitor.ecm.resource.cron}")
  public void ecmResourceTask() {
    String tenant = "";
    BigDecimal leftTotalMemory = new BigDecimal("0.0");
    BigDecimal leftTotalInstance = new BigDecimal("0.0");
    BigDecimal leftTotalCores = new BigDecimal("0.0");
    BigDecimal usedTotalMemory = new BigDecimal("0.0");
    BigDecimal usedTotalInstance = new BigDecimal("0.0");
    BigDecimal usedTotalCores = new BigDecimal("0.0");
    StringJoiner minorStr = new StringJoiner(",");
    StringJoiner majorStr = new StringJoiner(",");
    // 获取emNode资源信息
    List<Map<String, Object>> emNodeVoList = new ArrayList<>();
    try {
      Map<String, Object> resultmap = HttpsUntils.getEmsResourceList();
      // got interface data
      Map<String, List<Map<String, Object>>> data = MapUtils.getMap(resultmap, "data");
      emNodeVoList = data.getOrDefault("EMs", new ArrayList<>());
      logger.info("ResourceMonitor  response  {}:", resultmap);
    } catch (IOException e) {
      logger.warn("failed to get EcmResource data");
    }

    for (Map<String, Object> emNodeVoMap : emNodeVoList) {
      // 新增 ECM资源告警，需补充此ECM所属租户
      List<Map<String, Object>> labels = (List<Map<String, Object>>) emNodeVoMap.get("labels");
      for (Map<String, Object> labelMap : labels) {
        tenant = "租户标签：公共资源";
        if (labelMap.containsKey("tenant")) {
          tenant = "租户标签：" + labelMap.get("stringValue").toString();
        }
      }
      Map<String, Object> leftResourceMap = MapUtils.getMap(emNodeVoMap, "leftResource");
      // 获取剩余内存,实例，core
      String leftMemoryStr = leftResourceMap.getOrDefault("memory", "0").toString().trim();
      String leftCoresStr = leftResourceMap.getOrDefault("cores", "0").toString().trim();
      String leftInstanceStr = leftResourceMap.getOrDefault("instance", "0").toString().trim();

      BigDecimal leftMemory = new BigDecimal(leftMemoryStr);
      BigDecimal leftCores = new BigDecimal(leftCoresStr);
      BigDecimal leftInstance = new BigDecimal(leftInstanceStr);

      // 获取最大资源map
      Map<String, Object> maxResourceMap = MapUtils.getMap(emNodeVoMap, "maxResource");
      // 获取最大内存,实例，core
      String maxMemoryStr = maxResourceMap.getOrDefault("memory", "0").toString().trim();
      String maxCoresStr = maxResourceMap.getOrDefault("cores", "0").toString().trim();
      String maxInstanceStr = maxResourceMap.getOrDefault("instance", "0").toString().trim();

      BigDecimal maxMemory = new BigDecimal(maxMemoryStr);
      BigDecimal maxCores = new BigDecimal(maxCoresStr);
      BigDecimal maxInstance = new BigDecimal(maxInstanceStr);

      // 获取已使用资源map
      Map<String, Object> usedResourceMap = MapUtils.getMap(emNodeVoMap, "usedResource");
      // 获取已使用内存,实例，core
      String usedMemoryStr = usedResourceMap.getOrDefault("memory", "0").toString().trim();
      String usedCoresStr = usedResourceMap.getOrDefault("cores", "0").toString().trim();
      String usedInstanceStr = usedResourceMap.getOrDefault("instance", "0").toString().trim();

      BigDecimal usedMemory = new BigDecimal(usedMemoryStr);
      BigDecimal usedCores = new BigDecimal(usedCoresStr);
      BigDecimal usedInstance = new BigDecimal(usedInstanceStr);

      // 资源比例计算：剩余百分比
      double memorydouble = leftMemory.divide(maxMemory, 2, RoundingMode.HALF_DOWN).doubleValue();
      double coresdouble = leftCores.divide(maxCores, 2, RoundingMode.HALF_DOWN).doubleValue();
      double instancedouble =
          leftInstance.divide(maxInstance, 2, RoundingMode.HALF_DOWN).doubleValue();

      // 获取配置文件告警阈值
      Double majorValue = MonitorConfig.ECM_TASK_MAJOR.getValue();
      Double minorValue = MonitorConfig.ECM_TASK_MINOR.getValue();

      // 阈值对比
      if (((memorydouble) <= majorValue)
          || ((coresdouble) <= majorValue)
          || ((instancedouble) <= majorValue)) {
        majorStr.add(emNodeVoMap.get("instance").toString());
      } else if (((memorydouble) < minorValue)
          || ((coresdouble) < minorValue)
          || ((instancedouble) < minorValue)) {
        minorStr.add(emNodeVoMap.get("instance").toString());
      }

      // 发送告警
      HashMap<String, String> replaceParm = new HashMap<>();
      replaceParm.put("$tenant", tenant);
      if (StringUtils.isNotBlank(majorStr.toString())) {
        replaceParm.put("$instance", majorStr.toString());
        replaceParm.put("$ratio", majorValue.toString());
        Map<String, AlertDesc> ecmResourceAlerts =
            MonitorAlertUtils.getAlerts(Constants.ALERT_RESOURCE_MONITOR(), replaceParm);
        PooledImsAlertUtils.addAlert(ecmResourceAlerts.get("12004"));
      }
      if (StringUtils.isNotBlank(minorStr.toString())) {
        replaceParm.put("$instance", minorStr.toString());
        replaceParm.put("$ratio", minorValue.toString());
        Map<String, AlertDesc> ecmResourceAlerts =
            MonitorAlertUtils.getAlerts(Constants.ALERT_RESOURCE_MONITOR(), replaceParm);
        PooledImsAlertUtils.addAlert(ecmResourceAlerts.get("12003"));
      }
      // 发送IMS EM剩余资源百分比
      resourceSendToIms(coresdouble, memorydouble, instancedouble, HttpsUntils.localHost, "LEFT");

      // 收集所有EM剩余资源总数
      leftTotalMemory = leftTotalMemory.add(leftMemory);
      leftTotalCores = leftTotalCores.add(leftCores);
      leftTotalInstance = leftTotalInstance.add(leftInstance);

      // 收集所有EM已使用资源总数
      usedTotalMemory = usedTotalMemory.add(usedMemory);
      usedTotalCores = usedTotalCores.add(usedCores);
      usedTotalInstance = usedTotalInstance.add(usedInstance);
    }
    // 发送IMS EM剩余总资源
    resourceSendToIms(
        leftTotalCores.doubleValue(),
        leftTotalMemory.doubleValue(),
        leftTotalInstance.doubleValue(),
        HttpsUntils.localHost,
        "TOTAL_LEFT");
    resourceSendToIms(
        usedTotalCores.doubleValue(),
        usedTotalMemory.doubleValue(),
        usedTotalInstance.doubleValue(),
        HttpsUntils.localHost,
        "TOTAL_USED");
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
