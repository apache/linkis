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

package org.apache.linkis.cs.server.label;

import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.cs.server.conf.ContextServerConf;
import org.apache.linkis.instance.label.client.InstanceLabelClient;
import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.protocol.label.InsLabelRefreshRequest;
import org.apache.linkis.protocol.label.InsLabelRemoveRequest;
import org.apache.linkis.publicservice.common.lock.entity.CommonLock;
import org.apache.linkis.publicservice.common.lock.service.CommonLockService;
import org.apache.linkis.rpc.Sender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.AvailabilityState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.springframework.boot.availability.ReadinessState.ACCEPTING_TRAFFIC;

@Component
public class CSInstanceLabelClient {
  private static final Logger logger = LoggerFactory.getLogger(CSInstanceLabelClient.class);

  @Autowired CommonLockService commonLockService;

  private String _LOCK = "_MASTER_PS_CS_LABEL_LOCK";
  CommonLock commonLock = new CommonLock();
  private boolean lock = false;

  @EventListener(classes = {AvailabilityChangeEvent.class})
  public void init(AvailabilityChangeEvent<AvailabilityState> availabilityChangeEvent) {
    AvailabilityState state = availabilityChangeEvent.getState();
    logger.info("CSInstanceLabelClient app state {}", state);

    if (state instanceof ReadinessState && state == ACCEPTING_TRAFFIC) {
      Map<String, Object> labels = new HashMap<>(1);
      commonLock.setLockObject(_LOCK);
      commonLock.setCreateTime(new Date());
      commonLock.setUpdateTime(new Date());
      commonLock.setCreator(Utils.getJvmUser());
      commonLock.setLocker(Utils.getLocalHostname());
      commonLock.setUpdator(Utils.getJvmUser());
      lock = commonLockService.reentrantLock(commonLock, -1L);
      String suffix = ContextServerConf.CS_LABEL_SUFFIX;
      String confLabel;

      if (lock) {
        // master node set cs_1_xxx label
        logger.info("The master ps-cs node get lock by {}", _LOCK + "-" + commonLock.getLocker());
        confLabel = "cs_1_" + suffix;
      } else {
        confLabel = "cs_2_" + suffix;
      }
      logger.info("register label {} to ps-cs node.", confLabel);
      labels.put(LabelKeyConstant.ROUTE_KEY, confLabel);
      InsLabelRefreshRequest insLabelRefreshRequest = new InsLabelRefreshRequest();
      insLabelRefreshRequest.setLabels(labels);
      insLabelRefreshRequest.setServiceInstance(Sender.getThisServiceInstance());
      InstanceLabelClient.getInstance().refreshLabelsToInstance(insLabelRefreshRequest);
    }
  }

  @EventListener(classes = {ContextClosedEvent.class})
  public void shutdown(ContextClosedEvent contextClosedEvent) {
    logger.info("To remove labels for instance");
    InsLabelRemoveRequest insLabelRemoveRequest = new InsLabelRemoveRequest();
    insLabelRemoveRequest.setServiceInstance(Sender.getThisServiceInstance());
    InstanceLabelClient.getInstance().removeLabelsFromInstance(insLabelRemoveRequest);
    logger.info("success to send clear label rpc request");
    if (lock) {
      commonLockService.unlock(commonLock);
      logger.info(
          "The master ps-cs  node has released lock {}.",
          commonLock.getLockObject() + "-" + commonLock.getLocker());
    }
  }
}
