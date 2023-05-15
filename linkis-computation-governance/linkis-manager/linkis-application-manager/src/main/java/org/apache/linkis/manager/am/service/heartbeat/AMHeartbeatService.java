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

package org.apache.linkis.manager.am.service.heartbeat;

import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.manager.am.conf.ManagerMonitorConf;
import org.apache.linkis.manager.am.service.HeartbeatService;
import org.apache.linkis.manager.common.conf.RMConfiguration;
import org.apache.linkis.manager.common.entity.metrics.AMNodeMetrics;
import org.apache.linkis.manager.common.monitor.ManagerMonitor;
import org.apache.linkis.manager.common.protocol.node.NodeHeartbeatMsg;
import org.apache.linkis.manager.persistence.NodeManagerPersistence;
import org.apache.linkis.manager.persistence.NodeMetricManagerPersistence;
import org.apache.linkis.manager.service.common.metrics.MetricsConverter;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AMHeartbeatService implements HeartbeatService {
  private static final Logger logger = LoggerFactory.getLogger(AMHeartbeatService.class);

  @Autowired private NodeManagerPersistence nodeManagerPersistence;

  @Autowired private NodeMetricManagerPersistence nodeMetricManagerPersistence;

  @Autowired private MetricsConverter metricsConverter;

  @Autowired(required = false)
  private ManagerMonitor managerMonitor;

  @PostConstruct
  public void init() {
    if (managerMonitor != null && (boolean) ManagerMonitorConf.MONITOR_SWITCH_ON.getValue()) {
      logger.info("start init AMHeartbeatService monitor");
      Utils.defaultScheduler()
          .scheduleAtFixedRate(
              managerMonitor,
              1000,
              RMConfiguration.RM_ENGINE_SCAN_INTERVAL.getValue(),
              TimeUnit.MILLISECONDS);
    }
  }

  @Receiver
  @Override
  public void heartbeatEventDeal(NodeHeartbeatMsg nodeHeartbeatMsg) {
    AMNodeMetrics nodeMetrics = new AMNodeMetrics();
    logger.info("Am deal nodeHeartbeatMsg " + nodeHeartbeatMsg);
    nodeMetrics.setHealthy(metricsConverter.convertHealthyInfo(nodeHeartbeatMsg.getHealthyInfo()));
    nodeMetrics.setHeartBeatMsg(nodeHeartbeatMsg.getHeartBeatMsg());
    nodeMetrics.setOverLoad(
        metricsConverter.convertOverLoadInfo(nodeHeartbeatMsg.getOverLoadInfo()));
    nodeMetrics.setServiceInstance(nodeHeartbeatMsg.getServiceInstance());
    if (nodeHeartbeatMsg.getStatus() != null) {
      nodeMetrics.setStatus(metricsConverter.convertStatus(nodeHeartbeatMsg.getStatus()));
    } else {
      nodeMetrics.setStatus(0);
    }
    nodeMetricManagerPersistence.addOrupdateNodeMetrics(nodeMetrics);
    logger.info("Finished to deal nodeHeartbeatMsg {}", nodeHeartbeatMsg);
  }
}
