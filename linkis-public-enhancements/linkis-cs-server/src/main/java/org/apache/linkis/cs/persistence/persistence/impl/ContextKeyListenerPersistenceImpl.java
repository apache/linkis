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

import org.apache.linkis.cs.common.entity.listener.CommonContextKeyListenerDomain;
import org.apache.linkis.cs.common.entity.listener.ContextKeyListenerDomain;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.persistence.dao.ContextKeyListenerMapper;
import org.apache.linkis.cs.persistence.dao.ContextMapMapper;
import org.apache.linkis.cs.persistence.entity.PersistenceContextKeyListener;
import org.apache.linkis.cs.persistence.entity.PersistenceContextKeyValue;
import org.apache.linkis.cs.persistence.persistence.ContextKeyListenerPersistence;
import org.apache.linkis.cs.persistence.persistence.ContextMapPersistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ContextKeyListenerPersistenceImpl implements ContextKeyListenerPersistence {

  @Autowired private ContextKeyListenerMapper contextKeyListenerMapper;

  @Autowired private ContextMapMapper contextMapMapper;

  @Autowired private ContextMapPersistence contextMapPersistence;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public void create(ContextID contextID, ContextKeyListenerDomain contextKeyListenerDomain)
      throws CSErrorException {
    PersistenceContextKeyListener listener = new PersistenceContextKeyListener();
    listener.setSource(contextKeyListenerDomain.getSource());
    // contextKey 去数据库中查出响应的id
    PersistenceContextKeyValue contextMap =
        contextMapMapper.getContextMap(contextID, contextKeyListenerDomain.getContextKey());
    listener.setKeyId(contextMap.getId());
    contextKeyListenerMapper.createKeyListener(listener);
  }

  @Override
  public void remove(ContextID contextID, ContextKeyListenerDomain contextKeyListenerDomain)
      throws CSErrorException {
    PersistenceContextKeyValue contextMap =
        contextMapMapper.getContextMap(contextID, contextKeyListenerDomain.getContextKey());
    Integer keyId = contextMap.getId();
    contextKeyListenerMapper.remove(contextKeyListenerDomain, keyId);
  }

  @Override
  public void removeAll(ContextID contextID) throws CSErrorException {
    // 找到contextID 对应的所有map id
    List<Integer> keyIds =
        contextMapMapper.getAllContextMapByContextID(contextID).stream()
            .map(PersistenceContextKeyValue::getId)
            .collect(Collectors.toList());
    contextKeyListenerMapper.removeAll(keyIds);
  }

  @Override
  public List<ContextKeyListenerDomain> getAll(ContextID contextID) throws CSErrorException {
    List<PersistenceContextKeyValue> pKVs = contextMapMapper.getAllContextMapByContextID(contextID);
    List<Integer> keyIds =
        contextMapMapper.getAllContextMapByContextID(contextID).stream()
            .map(PersistenceContextKeyValue::getId)
            .collect(Collectors.toList());
    ArrayList<ContextKeyListenerDomain> domains = new ArrayList<>();
    if (!keyIds.isEmpty()) {
      logger.info("fetch %s keyIds by contextId %s", keyIds.size(), contextID.getContextId());
      List<PersistenceContextKeyListener> listeners = contextKeyListenerMapper.getAll(keyIds);
      for (PersistenceContextKeyListener listener : listeners) {
        domains.add(pDomainToCommon(listener, contextID, pKVs));
      }
    }
    return domains;
  }

  public ContextKeyListenerDomain pDomainToCommon(
      PersistenceContextKeyListener listener,
      ContextID contextID,
      List<PersistenceContextKeyValue> pKVs)
      throws CSErrorException {
    CommonContextKeyListenerDomain domain = new CommonContextKeyListenerDomain();
    domain.setSource(listener.getSource());
    Optional<PersistenceContextKeyValue> first =
        pKVs.stream().filter(kv -> kv.getId().equals(listener.getId())).findFirst();
    if (first.isPresent()) {
      ContextKeyValue contextKeyValue =
          contextMapPersistence.get(contextID, first.get().getContextKey());
      domain.setContextKey(contextKeyValue.getContextKey());
    }
    return domain;
  }

  @Override
  public ContextKeyListenerDomain getBy(ContextKeyListenerDomain contextKeyListenerDomain)
      throws CSErrorException {

    return contextKeyListenerDomain;
  }
}
