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

package org.apache.linkis.cs.server.service.impl;

import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.highavailable.ha.ContextHAChecker;
import org.apache.linkis.cs.persistence.ContextPersistenceManager;
import org.apache.linkis.cs.persistence.entity.PersistenceContextID;
import org.apache.linkis.cs.persistence.persistence.ContextIDPersistence;
import org.apache.linkis.cs.server.enumeration.ServiceType;
import org.apache.linkis.cs.server.service.ContextIDService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ContextIDServiceImpl extends ContextIDService {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired private ContextPersistenceManager persistenceManager;

  @Autowired private ContextHAChecker contextHAChecker;

  private ContextIDPersistence getPersistence() throws CSErrorException {
    return persistenceManager.getContextIDPersistence();
  }

  @Override
  public String getName() {
    return ServiceType.CONTEXT_ID.name();
  }

  @Override
  public String createContextID(ContextID contextID) throws CSErrorException {
    ContextID res = getPersistence().createContextID(contextID);
    logger.info(String.format("createContextID,csId:%s", res.getContextId()));
    return res.getContextId();
  }

  @Override
  public ContextID getContextID(String id) throws CSErrorException {
    logger.info(String.format("getContextID,csId:%s", id));
    return getPersistence().getContextID(id);
  }

  @Override
  public void updateContextID(ContextID contextID) throws CSErrorException {
    logger.info(String.format("updateContextID,csId:%s", contextID.getContextId()));
    getPersistence().updateContextID(contextID);
  }

  @Override
  public void resetContextID(String id) throws CSErrorException {
    // TODO: 2020/2/23 reset 方法
  }

  @Override
  public void removeContextID(String id) throws CSErrorException {
    logger.info(String.format("removeContextID,csId:%s", id));
    getPersistence().deleteContextID(id);
  }

  @Override
  public List<String> searchCSIDByTime(
      Date createTimeStart,
      Date createTimeEnd,
      Date updateTimeStart,
      Date updateTimeEnd,
      Date accessTimeStart,
      Date accessTimeEnd,
      Integer pageNow,
      Integer pageSize)
      throws CSErrorException {
    List<PersistenceContextID> rs = null;
    PageHelper.startPage(pageNow, pageSize);
    try {
      rs =
          getPersistence()
              .searchCSIDByTime(
                  createTimeStart,
                  createTimeEnd,
                  updateTimeStart,
                  updateTimeEnd,
                  accessTimeStart,
                  accessTimeEnd);
    } finally {
      PageHelper.clearPage();
    }
    PageInfo<PersistenceContextID> pageInfo = new PageInfo<>(rs);
    List<PersistenceContextID> pageResult = pageInfo.getList();
    List<String> result = new ArrayList<>();
    List<ContextID> errList = new ArrayList<>();
    if (null != pageResult)
      pageResult.stream()
          .forEach(
              (persistenceContextID -> {
                try {
                  result.add(contextHAChecker.convertHAIDToHAKey(persistenceContextID));
                } catch (CSErrorException e) {
                  logger.error(
                      "convert contextID to hdid failed. id : {}, source : {}",
                      persistenceContextID.getContextId(),
                      persistenceContextID.getSource());
                  errList.add(persistenceContextID);
                }
              }));
    if (errList.size() > 0) {
      throw new CSErrorException(
          97001,
          "There are "
              + errList.size()
              + " persistenceContextID that cannot be deserized from source.");
    }
    return result;
  }
}
