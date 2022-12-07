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
import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord;
import org.apache.linkis.manager.dao.ECResourceRecordMapper;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ECResourceInfoServiceImpl implements ECResourceInfoService {

  private static final Logger logger = LoggerFactory.getLogger(EMRestfulApi.class);

  @Autowired private ECResourceRecordMapper ecResourceRecordMapper;

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
}
