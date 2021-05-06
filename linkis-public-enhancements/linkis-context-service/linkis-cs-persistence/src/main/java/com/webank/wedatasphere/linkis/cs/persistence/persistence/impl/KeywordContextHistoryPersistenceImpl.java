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

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.history.ContextHistory;
import com.webank.wedatasphere.linkis.cs.common.entity.history.ContextHistoryIndexer;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.persistence.dao.ContextHistoryMapper;
import com.webank.wedatasphere.linkis.cs.persistence.entity.PersistenceContextHistory;
import com.webank.wedatasphere.linkis.cs.persistence.persistence.KeywordContextHistoryPersistence;
import com.webank.wedatasphere.linkis.cs.persistence.util.PersistenceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class KeywordContextHistoryPersistenceImpl implements KeywordContextHistoryPersistence {

    @Autowired
    private ContextHistoryMapper contextHistoryMapper;
    @Autowired
    private ContextHistoryPersistenceImpl contextHistoryPersistence;

    @Override
    public List<ContextHistory> search(ContextID contextID, String[] keywords) throws CSErrorException {
        List<PersistenceContextHistory> pHistory = contextHistoryMapper.searchByKeywords(contextID, keywords);
        return pHistory.stream().map(PersistenceUtils.map(contextHistoryPersistence::transfer)).collect(Collectors.toList());
    }

    @Override
    public List<ContextHistory> search(ContextType contextType, String[] keywords) throws CSErrorException {
        List<PersistenceContextHistory> pHistory = contextHistoryMapper.searchByKeywordsAndType(contextType, keywords);
        return pHistory.stream().map(PersistenceUtils.map(contextHistoryPersistence::transfer)).collect(Collectors.toList());
    }

    @Override
    public ContextHistoryIndexer getContextHistoryIndexer() throws CSErrorException {
        return null;
    }

    @Override
    public void setContextHistoryIndexer(ContextHistoryIndexer contextHistoryIndexer) throws CSErrorException {

    }
}
