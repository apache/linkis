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

package org.apache.linkis.cs.persistence.persistence.impl;

import org.apache.linkis.cs.common.entity.history.ContextHistory;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.persistence.dao.ContextHistoryMapper;
import org.apache.linkis.cs.persistence.entity.ExtraFieldClass;
import org.apache.linkis.cs.persistence.entity.PersistenceContextHistory;
import org.apache.linkis.cs.persistence.persistence.ContextHistoryPersistence;
import org.apache.linkis.cs.persistence.util.PersistenceUtils;
import org.apache.linkis.server.BDPJettyServerHelper;

import org.apache.commons.math3.util.Pair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ContextHistoryPersistenceImpl implements ContextHistoryPersistence {

  @Autowired private ContextHistoryMapper contextHistoryMapper;

  private ObjectMapper json = BDPJettyServerHelper.jacksonJson();

  private Class<PersistenceContextHistory> pClass = PersistenceContextHistory.class;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public void createHistory(ContextID contextID, ContextHistory contextHistory)
      throws CSErrorException {
    Pair<PersistenceContextHistory, ExtraFieldClass> pHistroy =
        PersistenceUtils.transfer(contextHistory, pClass);
    pHistroy.getFirst().setHistoryJson(PersistenceUtils.serialize(contextHistory));
    pHistroy.getFirst().setContextId(contextID.getContextId());
    contextHistoryMapper.createHistory(pHistroy.getFirst());
  }

  @Override
  public List<ContextHistory> getHistories(ContextID contextID) throws CSErrorException {
    List<PersistenceContextHistory> pHistories =
        contextHistoryMapper.getHistoriesByContextID(contextID);
    return pHistories.stream()
        .map(PersistenceUtils.map(this::transfer))
        .collect(Collectors.toList());
  }

  public ContextHistory transfer(PersistenceContextHistory pHistory) throws CSErrorException {
    ContextHistory history = PersistenceUtils.deserialize(pHistory.getHistoryJson());
    return history;
  }

  @Override
  public ContextHistory getHistory(ContextID contextID, Long id) throws CSErrorException {
    PersistenceContextHistory pHistory = contextHistoryMapper.getHistory(contextID, id);
    return pHistory == null ? null : transfer(pHistory);
  }

  @Override
  public ContextHistory getHistory(ContextID contextID, String source) throws CSErrorException {
    PersistenceContextHistory pHistory = contextHistoryMapper.getHistoryBySource(contextID, source);
    return pHistory == null ? null : transfer(pHistory);
  }

  @Override
  public void removeHistory(ContextID contextID, String source) throws CSErrorException {
    contextHistoryMapper.removeHistory(contextID, source);
  }

  @Override
  public void updateHistory(ContextID contextID, ContextHistory contextHistory)
      throws CSErrorException {
    Pair<PersistenceContextHistory, ExtraFieldClass> pHistroy =
        PersistenceUtils.transfer(contextHistory, pClass);
    contextHistoryMapper.updateHistory(contextID, pHistroy.getFirst());
  }
}
