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

package org.apache.linkis.manager.am.service.impl;

import org.apache.linkis.manager.am.conf.AMConfiguration;
import org.apache.linkis.manager.am.converter.MetricsConverter;
import org.apache.linkis.manager.am.service.EngineConnStatusCallbackService;
import org.apache.linkis.manager.am.service.engine.EngineStopService;
import org.apache.linkis.manager.common.constant.AMConstant;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.metrics.AMNodeMetrics;
import org.apache.linkis.manager.common.protocol.engine.EngineConnStatusCallback;
import org.apache.linkis.manager.common.protocol.engine.EngineConnStatusCallbackToAM;
import org.apache.linkis.manager.persistence.NodeMetricManagerPersistence;
import org.apache.linkis.rpc.message.annotation.Receiver;
import org.apache.linkis.server.BDPJettyServerHelper;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultEngineConnStatusCallbackService implements EngineConnStatusCallbackService {

  private static final Logger logger =
      LoggerFactory.getLogger(DefaultEngineConnStatusCallbackService.class);

  @Autowired private NodeMetricManagerPersistence nodeMetricManagerPersistence;

  @Autowired private MetricsConverter metricsConverter;

  @Autowired private EngineStopService engineStopService;

  private static final String[] canRetryLogs = AMConfiguration.AM_CAN_RETRY_LOGS.split(";");

  @Receiver
  public void dealEngineConnStatusCallback(EngineConnStatusCallback protocol) {
    logger.info(
        "EngineConnStatusCallbackServiceImpl handle engineConnStatus callback serviceInstance: [{}] status: [{}]",
        protocol.serviceInstance(),
        protocol.status());
    if (!NodeStatus.isAvailable(protocol.status())) {
      dealEngineConnStatusCallbackToAM(
          new EngineConnStatusCallbackToAM(
              protocol.serviceInstance(), protocol.status(), protocol.initErrorMsg(), false));
    }
  }

  @Receiver
  public void dealEngineConnStatusCallbackToAM(
      EngineConnStatusCallbackToAM engineConnStatusCallbackToAM) {
    if (engineConnStatusCallbackToAM.serviceInstance() == null) {
      logger.warn("call back service instance is null");
    }
    logger.info(
        "EngineConnStatusCallbackServiceImpl start to deal engineConnStatusCallbackToAM {}",
        engineConnStatusCallbackToAM);

    AMNodeMetrics nodeMetrics = new AMNodeMetrics();
    Map<String, Object> heartBeatMsg = new HashMap<>();
    int initErrorMsgMaxByteNum = 60000;

    String initErrorMsg = engineConnStatusCallbackToAM.initErrorMsg();
    try {
      if (StringUtils.isNotBlank(initErrorMsg)
          && initErrorMsg.getBytes("utf-8").length >= initErrorMsgMaxByteNum) {
        initErrorMsg = initErrorMsg.substring(0, initErrorMsgMaxByteNum);
      }
    } catch (UnsupportedEncodingException e) {
      logger.warn("dealEngineConnStatusCallbackToAM getBytes failed", e);
    }
    heartBeatMsg.put(AMConstant.START_REASON, initErrorMsg);

    if (engineConnStatusCallbackToAM.canRetry()) {
      heartBeatMsg.put(AMConstant.EC_CAN_RETRY, engineConnStatusCallbackToAM.canRetry());
    } else if (matchRetryLog(engineConnStatusCallbackToAM.initErrorMsg())) {
      logger.info("match canRetry log {}", engineConnStatusCallbackToAM.serviceInstance());
      heartBeatMsg.put(AMConstant.EC_CAN_RETRY, engineConnStatusCallbackToAM.canRetry());
    }

    try {
      nodeMetrics.setHeartBeatMsg(
          BDPJettyServerHelper.jacksonJson().writeValueAsString(heartBeatMsg));
    } catch (JsonProcessingException e) {
      logger.warn("dealEngineConnStatusCallbackToAM writeValueAsString failed", e);
    }
    nodeMetrics.setServiceInstance(engineConnStatusCallbackToAM.serviceInstance());
    nodeMetrics.setStatus(metricsConverter.convertStatus(engineConnStatusCallbackToAM.status()));

    nodeMetricManagerPersistence.addOrupdateNodeMetrics(nodeMetrics);
    logger.info("Finished to deal engineConnStatusCallbackToAM {}", engineConnStatusCallbackToAM);
  }

  private boolean matchRetryLog(String errorMsg) {
    boolean flag = false;
    if (StringUtils.isNotBlank(errorMsg)) {
      String errorMsgLowCase = errorMsg.toLowerCase(Locale.getDefault());
      for (String canRetry : canRetryLogs) {
        if (errorMsgLowCase.contains(canRetry)) {
          logger.info("match engineConn log fatal logs, is {}", canRetry);
          flag = true;
        }
      }
    }
    return flag;
  }
}
