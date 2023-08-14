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

package org.apache.linkis.manager.am.service.engine;

import org.apache.linkis.common.conf.Configuration$;
import org.apache.linkis.manager.am.conf.AMConfiguration;
import org.apache.linkis.manager.am.conf.EngineConnConfigurationService;
import org.apache.linkis.manager.common.entity.enumeration.MaintainType;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.protocol.engine.ECCanKillRequest;
import org.apache.linkis.manager.common.protocol.engine.ECCanKillResponse;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.manager.label.utils.LabelUtil;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultEngineConnCanKillService extends AbstractEngineService
    implements EngineConnCanKillService {
  private static final Logger logger =
      LoggerFactory.getLogger(DefaultEngineConnCanKillService.class);

  @Autowired private EngineConnConfigurationService engineConnConfigurationService;

  @Autowired private NodeLabelService labelService;

  @Receiver
  @Override
  public ECCanKillResponse canKillEngineConn(ECCanKillRequest ecCanKillRequest) {
    ECCanKillResponse ecCanKillResponse = new ECCanKillResponse();
    if (null == ecCanKillRequest
        || StringUtils.isBlank(ecCanKillRequest.getUser())
        || null == ecCanKillRequest.getEngineTypeLabel()) {
      ecCanKillResponse.setFlag(true);
      return ecCanKillResponse;
    }
    List<Label<?>> labels = new ArrayList<Label<?>>();
    labels.add(ecCanKillRequest.getEngineTypeLabel());
    labels.add(ecCanKillRequest.getUserCreatorLabel());
    MaintainType maintainType = MaintainType.Default;

    try {
      Map<String, String> configProps =
          engineConnConfigurationService.getConsoleConfiguration(labels);
      switch (AMConfiguration.EC_MAINTAIN_TIME_STR.getValue(configProps)) {
        case "周一到周五工作时间保持一个引擎":
          maintainType = MaintainType.day;
        case "周一到周五保持一个引擎":
          maintainType = MaintainType.week;
        default:
          maintainType = MaintainType.Default;
      }
    } catch (Exception e) {
      logger.warn("failed to get engine maintain time from publicservice", e);
      maintainType = MaintainType.Default;
    }

    switch (maintainType) {
      case day:
        return canKillInWorkTime(ecCanKillRequest);
      case week:
        return canKillInWorkDay(ecCanKillRequest);
      default:
        ecCanKillResponse.setFlag(true);
        ecCanKillResponse.setReason("default MaintainType");
        return ecCanKillResponse;
    }
  }

  private ECCanKillResponse canKillInWorkTime(ECCanKillRequest ecCanKillRequest) {
    if (isInWorkTime()) {
      return canKill(ecCanKillRequest);
    } else {
      ECCanKillResponse ecCanKillResponse = new ECCanKillResponse();
      ecCanKillResponse.setFlag(true);
      ecCanKillResponse.setReason("not workTime");
      return ecCanKillResponse;
    }
  }

  private ECCanKillResponse canKill(ECCanKillRequest ecCanKillRequest) {
    ECCanKillResponse ecCanKillResponse = new ECCanKillResponse();
    List<EngineNode> ecNodes =
        getEngineNodeManager().listEngines(ecCanKillRequest.getUser()).stream()
            .filter(
                node -> !node.getServiceInstance().equals(ecCanKillRequest.getEngineConnInstance()))
            .collect(Collectors.toList());
    if (ecNodes.isEmpty()) {
      ecCanKillResponse.setFlag(false);
      logger.info("There are no other engines, " + ecCanKillRequest + " can not be killed");
      ecCanKillResponse.setReason("There are no other engines");
      return ecCanKillResponse;
    }
    List<EngineNode> engineConnNodes =
        ecNodes.stream()
            .filter(node -> NodeStatus.isAvailable(node.getNodeStatus()))
            .collect(Collectors.toList());
    if (engineConnNodes.isEmpty()) {
      ecCanKillResponse.setFlag(false);
      logger.info(
          "There are no other engines available, " + ecCanKillRequest + " can not be killed");
      ecCanKillResponse.setReason("There are no other engines available");
      return ecCanKillResponse;
    }

    List<EngineNode> ecWithLabels =
        engineConnNodes.stream()
            .filter(
                node -> {
                  List<Label<?>> labels = labelService.getNodeLabels(node.getServiceInstance());
                  Label<?> userCreatorLabel = LabelUtil.getUserCreatorLabel(labels);
                  Label<?> engineTypeLabel = LabelUtil.getEngineTypeLabel(labels);
                  return ecCanKillRequest
                          .getUserCreatorLabel()
                          .getStringValue()
                          .equals(userCreatorLabel.getStringValue())
                      && ecCanKillRequest
                          .getEngineTypeLabel()
                          .getStringValue()
                          .equals(engineTypeLabel.getStringValue());
                })
            .collect(Collectors.toList());

    if (!ecWithLabels.isEmpty()) {
      ecCanKillResponse.setFlag(true);
      logger.info("There are engines available, " + ecCanKillRequest + " can be killed");
      ecCanKillResponse.setReason("There are engines available");
    } else {
      ecCanKillResponse.setFlag(false);
      logger.info(
          "There are no label equal engines available, " + ecCanKillRequest + " can not be killed");
      ecCanKillResponse.setReason("There are no label equal engines available");
    }
    return ecCanKillResponse;
  }

  private ECCanKillResponse canKillInWorkDay(ECCanKillRequest ecCanKillRequest) {
    if (isWeekend()) {
      ECCanKillResponse ecCanKillResponse = new ECCanKillResponse();
      ecCanKillResponse.setFlag(true);
      ecCanKillResponse.setReason("is weekend");
      return ecCanKillResponse;
    } else {
      return canKill(ecCanKillRequest);
    }
  }

  private Boolean isWeekend() {
    if ((boolean) Configuration$.MODULE$.IS_TEST_MODE().getValue()) {
      return true;
    }
    LocalDateTime date = LocalDateTime.now();
    DayOfWeek dayOfWeek = date.getDayOfWeek();
    return (dayOfWeek == DayOfWeek.SUNDAY) || (dayOfWeek == DayOfWeek.SATURDAY);
  }

  private Boolean isInWorkTime() {
    if (isWeekend()) {
      return false;
    }
    LocalDateTime date = LocalDateTime.now();
    return date.getHour() > AMConfiguration.EC_MAINTAIN_WORK_START_TIME
        && date.getHour() < AMConfiguration.EC_MAINTAIN_WORK_END_TIME;
  }
}
