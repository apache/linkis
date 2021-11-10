/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.cs.persistence.persistence.impl;

import org.apache.linkis.cs.common.entity.listener.CommonContextIDListenerDomain;
import org.apache.linkis.cs.common.entity.listener.ContextIDListenerDomain;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.persistence.dao.ContextIDListenerMapper;
import org.apache.linkis.cs.persistence.entity.PersistenceContextIDListener;
import org.apache.linkis.cs.persistence.persistence.ContextIDListenerPersistence;
import org.apache.linkis.cs.persistence.persistence.ContextIDPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class ContextIDListenerPersistenceImpl implements ContextIDListenerPersistence {

    @Autowired
    private ContextIDListenerMapper contextIDListenerMapper;

    @Autowired
    private ContextIDPersistence contextIDPersistence;

    @Override
    public void create(ContextID contextID, ContextIDListenerDomain contextIDListenerDomain) throws CSErrorException {
        PersistenceContextIDListener listener = new PersistenceContextIDListener();
        listener.setContextId(contextID.getContextId());
        listener.setSource(contextIDListenerDomain.getSource());
        contextIDListenerMapper.createIDListener(listener);
    }

    @Override
    public void remove(ContextIDListenerDomain contextIDListenerDomain) throws CSErrorException {
        // TODO: 2020/2/17
        PersistenceContextIDListener listener = new PersistenceContextIDListener();
        listener.setContextId(contextIDListenerDomain.getContextID().getContextId());
        listener.setSource(contextIDListenerDomain.getSource());
        contextIDListenerMapper.remove(listener);
    }

    @Override
    public void removeAll(ContextID contextID) throws CSErrorException {
        contextIDListenerMapper.removeAll(contextID);
    }

    @Override
    public List<ContextIDListenerDomain> getAll(ContextID contextID) throws CSErrorException {
        // 根据id返回一堆的domain
        ContextID complete = contextIDPersistence.getContextID(contextID.getContextId());
        List<PersistenceContextIDListener> listeners = contextIDListenerMapper.getAll(contextID);
        List<ContextIDListenerDomain> domains = listeners.stream().map(l -> pDomainToCommon(l, complete)).collect(Collectors.toList());
        return domains;
    }

    public ContextIDListenerDomain pDomainToCommon(PersistenceContextIDListener listener, ContextID contextID) {
        CommonContextIDListenerDomain domain = new CommonContextIDListenerDomain();
        domain.setContextID(contextID);
        domain.setSource(listener.getSource());
        return domain;
    }

    @Override
    public ContextIDListenerDomain getBy(ContextIDListenerDomain contextIDListenerDomain) throws CSErrorException {
        //根据id 和source 返回响应的ContextIDListenerDomain
        return contextIDListenerDomain;
    }
}
