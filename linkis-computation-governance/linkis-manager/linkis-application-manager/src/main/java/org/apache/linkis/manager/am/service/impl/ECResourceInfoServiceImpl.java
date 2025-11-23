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

import org.apache.linkis.manager.am.restful.EMRestfulApi;
import org.apache.linkis.manager.am.service.ECResourceInfoService;
import org.apache.linkis.manager.am.service.em.EMInfoService;
import org.apache.linkis.manager.am.util.ECResourceInfoUtils;
import org.apache.linkis.manager.am.utils.AMUtils;
import org.apache.linkis.manager.am.vo.EMNodeVo;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord;
import org.apache.linkis.manager.common.entity.persistence.PersistencerEcNodeInfo;
import org.apache.linkis.manager.dao.ECResourceRecordMapper;
import org.apache.linkis.manager.dao.LabelManagerMapper;
import org.apache.linkis.manager.dao.NodeManagerMapper;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.manager.persistence.LabelManagerPersistence;
import org.apache.linkis.protocol.constants.TaskConstant;
import org.apache.linkis.server.BDPJettyServerHelper;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ECResourceInfoServiceImpl implements ECResourceInfoService {

  private static final Logger logger = LoggerFactory.getLogger(EMRestfulApi.class);

  private ObjectMapper json =
      new ObjectMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

  @Autowired private ECResourceRecordMapper ecResourceRecordMapper;

  @Autowired private NodeManagerMapper nodeManagerMapper;

  @Autowired private LabelManagerMapper labelManagerMapper;

  @Autowired private LabelManagerPersistence labelManagerPersistence;

  @Autowired private NodeLabelService nodeLabelService;

  @Autowired private EMInfoService emInfoService;

  @Override
  public ECResourceInfoRecord getECResourceInfoRecord(String ticketId) {
    if (StringUtils.isNotBlank(ticketId)) {
      return ecResourceRecordMapper.getECResourceInfoRecord(ticketId);
    }
    return null;
  }

  @Override
  public ECResourceInfoRecord getECResourceInfoRecordByInstance(String instance) {
    if (StringUtils.isNotBlank(instance)) {
      return ecResourceRecordMapper.getECResourceInfoRecordByInstance(instance);
    }
    return null;
  }

  @Override
  public void deleteECResourceInfoRecordByTicketId(String ticketId) {
    if (StringUtils.isNotBlank(ticketId)) {
      logger.info("Start to delete ec：{} info ", ticketId);
      ecResourceRecordMapper.deleteECResourceInfoRecordByTicketId(ticketId);
    }
  }

  @Override
  public void deleteECResourceInfoRecord(Integer id) {
    logger.info("Start to delete ec id：{} info ", id);
    ecResourceRecordMapper.deleteECResourceInfoRecord(id);
  }

  @Override
  public List<ECResourceInfoRecord> getECResourceInfoRecordList(
      String instance,
      Date endDate,
      Date startDate,
      String username,
      String engineType,
      String status) {
    return ecResourceRecordMapper.getECResourceInfoHistory(
        username, instance, endDate, startDate, engineType, status);
  }

  @Override
  public List<Map<String, Object>> getECResourceInfoList(
      List<String> creatorUserList,
      List<String> engineTypeList,
      List<String> statusStrList,
      String queueName,
      List<String> ecInstancesList,
      Boolean isCrossCluster) {

    List<Map<String, Object>> resultList = new ArrayList<>();

    // convert status string to int
    List<Integer> statusIntList = new ArrayList<>();
    for (String status : statusStrList) {
      statusIntList.add(NodeStatus.valueOf(status).ordinal());
    }

    // get engine conn info list filter by creator user list /instance status list
    List<PersistencerEcNodeInfo> ecNodesInfo =
        nodeManagerMapper.getEMNodeInfoList(creatorUserList, statusIntList, ecInstancesList);

    // map k:v---> instanceName：PersistencerEcNodeInfo
    Map<String, PersistencerEcNodeInfo> persistencerEcNodeInfoMap =
        ecNodesInfo.stream()
            .collect(Collectors.toMap(PersistencerEcNodeInfo::getInstance, item -> item));

    List<String> instanceList =
        ecNodesInfo.stream().map(e -> e.getInstance()).collect(Collectors.toList());
    if (instanceList.size() == 0) {
      return resultList;
    }

    // filter by engineType and get latest resource record info
    List<ECResourceInfoRecord> ecResourceInfoRecords =
        ecResourceRecordMapper.getECResourceInfoList(instanceList, engineTypeList);

    ecResourceInfoRecords.forEach(
        latestRecord -> {
          PersistencerEcNodeInfo ecNodeinfo =
              persistencerEcNodeInfoMap.get(latestRecord.getServiceInstance());
          if (ecNodeinfo == null) {
            logger.info("Can not get any ec node info of ec:{}", ecNodeinfo.getInstance());
          } else {
            try {
              Map<String, Object> item =
                  json.readValue(
                      json.writeValueAsString(ecNodeinfo),
                      new TypeReference<Map<String, Object>>() {});

              Integer instanceStatus = ecNodeinfo.getInstanceStatus();
              item.put("instanceStatus", NodeStatus.values()[instanceStatus].name());

              String usedResourceStr = latestRecord.getUsedResource();
              /*
              {"instance":1,"memory":"2.0 GB","cpu":1}
              ->
              {"driver":{"instance":1,"memory":"2.0 GB","cpu":1} }
               */
              long lastUnlockTimestamp = 0L;
              if (NodeStatus.values()[instanceStatus].name().equals(NodeStatus.Unlock.name())) {
                String heartbeatMsg = ecNodeinfo.getHeartbeatMsg();
                Map<String, Object> heartbeatMap = new HashMap<>();
                if (StringUtils.isNotBlank(heartbeatMsg)) {
                  heartbeatMap =
                      BDPJettyServerHelper.gson()
                          .fromJson(heartbeatMsg, new HashMap<>().getClass());
                }
                Object lastUnlockTimestampObject =
                    heartbeatMap.getOrDefault("lastUnlockTimestamp", 0);
                BigDecimal lastUnlockTimestampBigDecimal =
                    new BigDecimal(String.valueOf(lastUnlockTimestampObject));
                lastUnlockTimestamp = lastUnlockTimestampBigDecimal.longValue();
              }
              item.put("lastUnlockTimestamp", lastUnlockTimestamp);
              item.put("useResource", ECResourceInfoUtils.getStringToMap(usedResourceStr));
              item.put(TaskConstant.ECM_INSTANCE, latestRecord.getEcmInstance());
              String engineType = latestRecord.getEngineType();
              item.put("engineType", engineType);
              if (StringUtils.isNotBlank(queueName)) {
                Map<String, Object> usedResourceMap =
                    ECResourceInfoUtils.getStringToMap(usedResourceStr);
                Map yarn = MapUtils.getMap(usedResourceMap, "yarn", new HashMap<String, Object>());
                String queueNameStr = String.valueOf(yarn.getOrDefault("queueName", ""));
                if (StringUtils.isNotBlank(queueNameStr) && queueName.equals(queueNameStr)) {
                  resultList.add(item);
                }
              } else {
                resultList.add(item);
              }
            } catch (JsonProcessingException e) {
              logger.error("Fail to process the ec node info: [{}]", ecNodeinfo, e);
            }
          }
        });
    if (null != isCrossCluster) {
      List<Map<String, Object>> resultListByCluster = new ArrayList<>();
      List<EMNodeVo> emNodeVos = AMUtils.copyToEMVo(emInfoService.getAllEM());
      Map<String, EMNodeVo> clusterMap =
          emNodeVos.stream()
              .filter(
                  s -> s.getLabels().stream().anyMatch(d -> d.getLabelKey().equals("yarnCluster")))
              .collect((Collectors.toMap(EMNodeVo::getInstance, item -> item)));
      for (Map<String, Object> stringObjectMap : resultList) {
        if (isCrossCluster
            && clusterMap.containsKey(stringObjectMap.get(TaskConstant.ECM_INSTANCE).toString())) {
          resultListByCluster.add(stringObjectMap);
        } else if (!isCrossCluster
            && !clusterMap.containsKey(stringObjectMap.get(TaskConstant.ECM_INSTANCE).toString())) {
          resultListByCluster.add(stringObjectMap);
        }
      }
      resultList.clear();
      resultList.addAll(resultListByCluster);
    }
    return resultList;
  }
}
