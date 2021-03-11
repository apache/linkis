/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.cs.persistence.persistence.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wedatasphere.linkis.cs.common.entity.history.ContextHistory;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.persistence.dao.ContextHistoryMapper;
import com.webank.wedatasphere.linkis.cs.persistence.entity.ExtraFieldClass;
import com.webank.wedatasphere.linkis.cs.persistence.entity.PersistenceContextHistory;
import com.webank.wedatasphere.linkis.cs.persistence.persistence.ContextHistoryPersistence;
import com.webank.wedatasphere.linkis.cs.persistence.util.PersistenceUtils;
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class ContextHistoryPersistenceImpl implements ContextHistoryPersistence {

    @Autowired
    private ContextHistoryMapper contextHistoryMapper;

    private ObjectMapper json = BDPJettyServerHelper.jacksonJson();

    private Class<PersistenceContextHistory> pClass = PersistenceContextHistory.class;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void createHistory(ContextID contextID, ContextHistory contextHistory) throws CSErrorException {
        Pair<PersistenceContextHistory, ExtraFieldClass> pHistroy = PersistenceUtils.transfer(contextHistory, pClass);
        pHistroy.getFirst().setHistoryJson(PersistenceUtils.serialize(contextHistory));
        pHistroy.getFirst().setContextId(contextID.getContextId());
        contextHistoryMapper.createHistory(pHistroy.getFirst());
    }

    @Override
    public List<ContextHistory> getHistories(ContextID contextID) throws CSErrorException {
        List<PersistenceContextHistory> pHistories = contextHistoryMapper.getHistoriesByContextID(contextID);
        return pHistories.stream().map(PersistenceUtils.map(this::transfer)).collect(Collectors.toList());
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
    public void updateHistory(ContextID contextID, ContextHistory contextHistory) throws CSErrorException {
        Pair<PersistenceContextHistory, ExtraFieldClass> pHistroy = PersistenceUtils.transfer(contextHistory, pClass);
        contextHistoryMapper.updateHistory(contextID, pHistroy.getFirst());
    }
}
