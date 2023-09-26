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

package org.apache.linkis.manager.rm.service.impl;

import org.apache.linkis.governance.common.utils.ECPathUtils;
import org.apache.linkis.manager.am.util.LinkisUtils;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord;
import org.apache.linkis.manager.common.entity.resource.Resource;
import org.apache.linkis.manager.dao.ECResourceRecordMapper;
import org.apache.linkis.manager.label.entity.CombinedLabel;
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel;
import org.apache.linkis.manager.label.entity.engine.EngineInstanceLabel;
import org.apache.linkis.manager.rm.domain.RMLabelContainer;
import org.apache.linkis.manager.rm.utils.RMUtils;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ResourceLogService {
  private static final Logger logger = LoggerFactory.getLogger(ResourceLogService.class);

  @Autowired private ECResourceRecordMapper ecResourceRecordMapper;

  private String printLog(
      String changeType,
      Resource resource,
      String status,
      EngineInstanceLabel engineLabel,
      EMInstanceLabel ecmLabel) {
    StringBuilder logString = new StringBuilder(changeType + " ");
    logString.append(status).append(", ");
    if (engineLabel != null && resource != null) {
      logString
          .append("engine current resource:")
          .append(engineLabel.getServiceInstance().getInstance());
      logString.append(resource.toJson()).append(" ");
    }
    if (ecmLabel != null && resource != null) {
      logString.append("ecm current resource:").append(ecmLabel.getServiceInstance().getInstance());
      logString.append(resource.toJson()).append(" ");
    }
    return logString.toString();
  }

  public void failed(
      String changeType,
      Resource resource,
      EngineInstanceLabel engineLabel,
      EMInstanceLabel ecmLabel,
      Exception exception) {
    if (StringUtils.isNotEmpty(changeType)) {
      String log;
      switch (changeType) {
        case ChangeType.ENGINE_INIT:
        case ChangeType.ENGINE_CLEAR:
          log = printLog(changeType, resource, ChangeType.FAILED, engineLabel, ecmLabel);
          break;
        case ChangeType.ECM_INIT:
        case ChangeType.ECM_CLEAR:
          log = printLog(changeType, resource, ChangeType.FAILED, null, ecmLabel);
          break;
        case ChangeType.ECM_RESOURCE_ADD:
        case ChangeType.ECM_Resource_MINUS:
          log = printLog(changeType, resource, ChangeType.FAILED, engineLabel, ecmLabel);
          break;
        default:
          log = " ";
          break;
      }
      if (exception != null) {
        logger.error(log, exception);
      } else {
        logger.error(log);
      }
    }
  }

  public void success(
      String changeType,
      Resource resource,
      EngineInstanceLabel engineLabel,
      EMInstanceLabel ecmLabel) {
    if (StringUtils.isNotEmpty(changeType)) {
      String log;
      switch (changeType) {
        case ChangeType.ENGINE_INIT:
        case ChangeType.ENGINE_CLEAR:
          log = printLog(changeType, resource, ChangeType.SUCCESS, engineLabel, ecmLabel);
          break;
        case ChangeType.ECM_INIT:
        case ChangeType.ECM_CLEAR:
          log = printLog(changeType, resource, ChangeType.SUCCESS, null, ecmLabel);
          break;
        case ChangeType.ECM_RESOURCE_ADD:
        case ChangeType.ECM_Resource_MINUS:
          log = printLog(changeType, resource, ChangeType.SUCCESS, engineLabel, ecmLabel);
          break;
        default:
          log = " ";
          break;
      }
      logger.info(log);
    }
  }

  public void printUsedResourceNode(EngineInstanceLabel nodeLabel, CombinedLabel source) {
    printNode(nodeLabel, source);
  }

  public void printReleaseResourceNode(EngineInstanceLabel nodeLabel, CombinedLabel source) {
    printNode(nodeLabel, source);
  }

  private void printNode(EngineInstanceLabel nodeLabel, CombinedLabel source) {
    String log = nodeLabel.getInstance() + "	" + source.getStringValue();
    logger.info(log);
  }

  public void recordUserResourceAction(
      RMLabelContainer labelContainer,
      String ticketId,
      String changeType,
      Resource resource,
      NodeStatus status,
      String metrics) {
    if (RMUtils.RM_RESOURCE_ACTION_RECORD.getValue()) {
      LinkisUtils.tryAndWarn(
          () -> {
            CombinedLabel combinedLabel = labelContainer.getCombinedResourceLabel();
            EngineInstanceLabel engineInstanceLabel = labelContainer.getEngineInstanceLabel();
            EMInstanceLabel eMInstanceLabel = labelContainer.getEMInstanceLabel();
            if (combinedLabel == null) {
              return;
            }
            ECResourceInfoRecord ecResourceInfoRecord =
                ecResourceRecordMapper.getECResourceInfoRecord(ticketId);
            if (ecResourceInfoRecord == null) {
              String logDirSuffix =
                  ECPathUtils.getECLogDirSuffix(
                      labelContainer.getEngineTypeLabel(),
                      labelContainer.getUserCreatorLabel(),
                      ticketId);
              String user =
                  labelContainer.getUserCreatorLabel() != null
                      ? labelContainer.getUserCreatorLabel().getUser()
                      : "";
              ecResourceInfoRecord =
                  new ECResourceInfoRecord(
                      combinedLabel.getStringValue(), user, ticketId, resource, logDirSuffix);
              ecResourceRecordMapper.insertECResourceInfoRecord(ecResourceInfoRecord);
            }
            if (engineInstanceLabel != null) {
              ecResourceInfoRecord.setServiceInstance(engineInstanceLabel.getInstance());
            }
            if (eMInstanceLabel != null) {
              ecResourceInfoRecord.setEcmInstance(eMInstanceLabel.getInstance());
            }
            switch (changeType) {
              case ChangeType.ENGINE_REQUEST:
                ecResourceInfoRecord.setRequestTimes(ecResourceInfoRecord.getRequestTimes() + 1);
                if (resource != null) {
                  ecResourceInfoRecord.setRequestResource(resource.toJson());
                }
                break;
              case ChangeType.ENGINE_INIT:
                ecResourceInfoRecord.setUsedTimes(ecResourceInfoRecord.getUsedTimes() + 1);
                if (resource != null) {
                  ecResourceInfoRecord.setUsedResource(resource.toJson());
                }
                ecResourceInfoRecord.setUsedTime(new Date(System.currentTimeMillis()));
                break;
              case ChangeType.ENGINE_CLEAR:
                ecResourceInfoRecord.setReleaseTimes(ecResourceInfoRecord.getReleaseTimes() + 1);
                if (resource != null) {
                  ecResourceInfoRecord.setReleasedResource(resource.toJson());
                }
                ecResourceInfoRecord.setReleaseTime(new Date(System.currentTimeMillis()));
                if (StringUtils.isNotBlank(metrics)) {
                  ecResourceInfoRecord.setMetrics(metrics);
                }
                break;
            }
            if (StringUtils.isBlank(ecResourceInfoRecord.getStatus())
                || !NodeStatus.isCompleted(
                    NodeStatus.toNodeStatus(ecResourceInfoRecord.getStatus()))) {
              ecResourceInfoRecord.setStatus(status.toString());
            }
            ecResourceRecordMapper.updateECResourceInfoRecord(ecResourceInfoRecord);
          },
          logger);
    }
  }
}
