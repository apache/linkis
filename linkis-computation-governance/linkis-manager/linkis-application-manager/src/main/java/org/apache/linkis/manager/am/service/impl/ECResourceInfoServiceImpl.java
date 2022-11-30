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
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord;
import org.apache.linkis.manager.common.entity.persistence.PersistencerEcNodeInfo;
import org.apache.linkis.manager.dao.ECResourceRecordMapper;
import org.apache.linkis.manager.dao.NodeManagerMapper;
import org.apache.linkis.server.BDPJettyServerHelper;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

  private ObjectMapper json = BDPJettyServerHelper.jacksonJson();

  @Autowired private ECResourceRecordMapper ecResourceRecordMapper;

  @Autowired private NodeManagerMapper nodeManagerMapper;

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
      String instance, Date endDate, Date startDate, String username, String engineType) {
    return ecResourceRecordMapper.getECResourceInfoHistory(
        username, instance, endDate, startDate, engineType);
  }

  @Override
  public List<Map<String, Object>> getECResourceInfoList(
      List<String> creatorUserList, List<String> engineTypeList, List<String> statusList) {

    List<Map<String, Object>> resultList = new ArrayList<>();

    // get engine conn info list filter by creator user list /engineType/instance status list
    List<PersistencerEcNodeInfo> ecNodesInfo =
        nodeManagerMapper.getEMNodeInfoList(creatorUserList, statusList);

    List<String> instanceList =
        ecNodesInfo.stream().map(e -> e.getInstance()).collect(Collectors.toList());

    if (instanceList.size() == 0) {
      return resultList;
    }

    // filter by engineType and get latest resource record info
    List<ECResourceInfoRecord> ecResourceInfoRecords =
        ecResourceRecordMapper.getECResourceInfoList(instanceList, engineTypeList);
    // map k:v---> instanceName：ECResourceInfoRecord
    Map<String, ECResourceInfoRecord> map =
        ecResourceInfoRecords.stream()
            .collect(Collectors.toMap(ECResourceInfoRecord::getServiceInstance, item -> item));

    ecNodesInfo.forEach(
        info -> {
          try {
            Map<String, Object> item =
                json.readValue(
                    json.writeValueAsString(info), new TypeReference<Map<String, Object>>() {});
            Integer intStatus = info.getInstanceStatus();
            item.put("instanceStatus", NodeStatus.values()[intStatus].name());
            ECResourceInfoRecord latestEcInfo = map.get(info.getInstance());
            item.put("useResource", latestEcInfo.getUsedResource());
            item.put("ecmInstance", latestEcInfo.getEcmInstance());
            String engineType = latestEcInfo.getLabelValue().split(",")[1].split("-")[0];
            item.put("engineType", engineType);

            resultList.add(item);

          } catch (JsonProcessingException e) {
            logger.error("Fail to process the ec info: [{}]", info, e);
          }
        });

    return resultList;
  }
}
