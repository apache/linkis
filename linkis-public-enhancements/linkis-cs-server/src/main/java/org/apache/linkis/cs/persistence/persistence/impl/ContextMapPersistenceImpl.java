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

import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.entity.source.ContextValue;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.common.serialize.helper.ContextSerializationHelper;
import org.apache.linkis.cs.common.serialize.helper.SerializationHelper;
import org.apache.linkis.cs.persistence.conf.PersistenceConf;
import org.apache.linkis.cs.persistence.dao.ContextMapMapper;
import org.apache.linkis.cs.persistence.entity.ExtraFieldClass;
import org.apache.linkis.cs.persistence.entity.PersistenceContextKey;
import org.apache.linkis.cs.persistence.entity.PersistenceContextKeyValue;
import org.apache.linkis.cs.persistence.entity.PersistenceContextValue;
import org.apache.linkis.cs.persistence.persistence.ContextMapPersistence;
import org.apache.linkis.cs.persistence.util.PersistenceUtils;
import org.apache.linkis.server.BDPJettyServerHelper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ContextMapPersistenceImpl implements ContextMapPersistence {

  @Autowired private ContextMapMapper contextMapMapper;

  private Class<PersistenceContextKey> pKClass = PersistenceContextKey.class;

  private Class<PersistenceContextValue> pVClass = PersistenceContextValue.class;

  private Class<PersistenceContextKeyValue> pKVClass = PersistenceContextKeyValue.class;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private ObjectMapper json = BDPJettyServerHelper.jacksonJson();

  private final SerializationHelper serialHelper = ContextSerializationHelper.getInstance();

  @Override
  public void create(ContextID contextID, ContextKeyValue kV) throws CSErrorException {
    // TODO: 2020/2/17 keywords 如何合并
    try {
      Pair<PersistenceContextKey, ExtraFieldClass> pK =
          PersistenceUtils.transfer(kV.getContextKey(), pKClass);
      Pair<PersistenceContextValue, ExtraFieldClass> pV =
          PersistenceUtils.transfer(kV.getContextValue(), pVClass);
      Pair<PersistenceContextKeyValue, ExtraFieldClass> pKV =
          PersistenceUtils.transfer(kV, pKVClass);
      pV.getFirst().setValueStr(serialHelper.serialize(kV.getContextValue().getValue()));
      pKV.getSecond().addSub(pK.getSecond());
      pKV.getSecond().addSub(pV.getSecond());
      pKV.getFirst().setProps(json.writeValueAsString(pKV.getSecond()));
      pKV.getFirst().setContextId(contextID.getContextId());
      pKV.getFirst().setContextKey(pK.getFirst());
      pKV.getFirst().setContextValue(pV.getFirst());
      Date now = new Date();
      pKV.getFirst().setCreateTime(now);
      pKV.getFirst().setUpdateTime(now);
      pKV.getFirst().setAccessTime(now);
      contextMapMapper.createMap(pKV.getFirst());
    } catch (JsonProcessingException e) {
      logger.error("writeAsJson failed:", e);
      throw new CSErrorException(97000, e.getMessage());
    }
  }

  @Override
  public void update(ContextID contextID, ContextKeyValue kV) throws CSErrorException {
    // 根据contextId和key 进行更新
    Pair<PersistenceContextKey, ExtraFieldClass> pK =
        PersistenceUtils.transfer(kV.getContextKey(), pKClass);
    Pair<PersistenceContextValue, ExtraFieldClass> pV =
        PersistenceUtils.transfer(kV.getContextValue(), pVClass);
    Pair<PersistenceContextKeyValue, ExtraFieldClass> pKV = PersistenceUtils.transfer(kV, pKVClass);
    Object value = kV.getContextValue().getValue();
    if (value != null) {
      pV.getFirst().setValueStr(serialHelper.serialize(value));
    }
    pKV.getFirst().setContextKey(pK.getFirst());
    pKV.getFirst().setContextValue(pV.getFirst());
    pKV.getFirst().setContextId(contextID.getContextId());
    if (null == pKV.getFirst().getAccessTime()) {
      pKV.getFirst().setUpdateTime(new Date());
    }
    contextMapMapper.updateMap(pKV.getFirst());
  }

  @Override
  public ContextKeyValue get(ContextID contextID, ContextKey contextKey) throws CSErrorException {
    // 根据contextId 和key,获取到一个ContextKeyValue
    PersistenceContextKeyValue pKV = contextMapMapper.getContextMap(contextID, contextKey);
    if (pKV == null) return null;
    return transfer(pKV);
  }

  private ContextKeyValue transfer(PersistenceContextKeyValue pKV) throws CSErrorException {
    try {
      // TODO: 2020/2/14 null return
      PersistenceContextKey pK = (PersistenceContextKey) pKV.getContextKey();
      PersistenceContextValue pV = (PersistenceContextValue) pKV.getContextValue();
      if (PersistenceConf.ENABLE_CS_DESERIALIZE_REPLACE_PACKAGE_HEADER.getValue()) {
        if (StringUtils.isBlank(pKV.getProps())
            || StringUtils.isBlank(
                PersistenceConf.CS_DESERIALIZE_REPLACE_PACKAGE_HEADER.getValue())) {
          logger.error(
              "Props : {} of ContextMap or CSID_REPLACE_PACKAGE_HEADER : {} cannot be empty.",
              pKV.getProps(),
              PersistenceConf.CS_DESERIALIZE_REPLACE_PACKAGE_HEADER.getValue());
        } else {
          if (pKV.getProps()
              .contains(PersistenceConf.CS_DESERIALIZE_REPLACE_PACKAGE_HEADER.getValue())) {
            if (logger.isDebugEnabled()) {
              logger.debug(
                  "Will replace package header of source : {} from : {} to : {}",
                  pKV.getProps(),
                  PersistenceConf.CS_DESERIALIZE_REPLACE_PACKAGE_HEADER.getValue(),
                  PersistenceConf.CSID_PACKAGE_HEADER);
            }
            pKV.setProps(
                pKV.getProps()
                    .replaceAll(
                        PersistenceConf.CS_DESERIALIZE_REPLACE_PACKAGE_HEADER.getValue(),
                        PersistenceConf.CSID_PACKAGE_HEADER));
          }
        }
      }
      ExtraFieldClass extraFieldClass = json.readValue(pKV.getProps(), ExtraFieldClass.class);
      ContextKey key = PersistenceUtils.transfer(extraFieldClass.getOneSub(0), pK);
      ContextValue value = PersistenceUtils.transfer(extraFieldClass.getOneSub(1), pV);
      if (value != null) {
        value.setValue(serialHelper.deserialize(pV.getValueStr()));
      }
      ContextKeyValue kv = PersistenceUtils.transfer(extraFieldClass, pKV);
      kv.setContextKey(key);
      kv.setContextValue(value);
      return kv;
    } catch (IOException e) {
      logger.error("readJson failed:", e);
      throw new CSErrorException(97000, e.getMessage());
    }
  }

  @Override
  public List<ContextKeyValue> getAll(ContextID contextID, String key) {
    // 模糊匹配key
    List<PersistenceContextKeyValue> pKVs = contextMapMapper.getAllContextMapByKey(contextID, key);
    return pKVs.stream().map(PersistenceUtils.map(this::transfer)).collect(Collectors.toList());
  }

  @Override
  public List<ContextKeyValue> getAll(ContextID contextID) {
    List<PersistenceContextKeyValue> pKVs = contextMapMapper.getAllContextMapByContextID(contextID);
    return pKVs.stream().map(PersistenceUtils.map(this::transfer)).collect(Collectors.toList());
  }

  @Override
  public List<ContextKeyValue> getAll(ContextID contextID, ContextScope contextScope) {
    List<PersistenceContextKeyValue> pKVs =
        contextMapMapper.getAllContextMapByScope(contextID, contextScope);
    return pKVs.stream().map(PersistenceUtils.map(this::transfer)).collect(Collectors.toList());
  }

  @Override
  public List<ContextKeyValue> getAll(ContextID contextID, ContextType contextType) {
    List<PersistenceContextKeyValue> pKVs =
        contextMapMapper.getAllContextMapByType(contextID, contextType);
    return pKVs.stream().map(PersistenceUtils.map(this::transfer)).collect(Collectors.toList());
  }

  @Override
  public void reset(ContextID contextID, ContextKey contextKey) {}

  @Override
  public void remove(ContextID contextID, ContextKey contextKey) {
    contextMapMapper.removeContextMap(contextID, contextKey);
  }

  @Override
  public void removeAll(ContextID contextID) {
    contextMapMapper.removeAllContextMapByContextID(contextID);
  }

  @Override
  public void removeAll(ContextID contextID, ContextType contextType) {
    contextMapMapper.removeAllContextMapByType(contextID, contextType);
  }

  @Override
  public void removeAll(ContextID contextID, ContextScope contextScope) {
    contextMapMapper.removeAllContextMapByScope(contextID, contextScope);
  }

  @Override
  public void removeByKeyPrefix(ContextID contextID, String keyPrefix) {
    contextMapMapper.removeByKeyPrefix(contextID, keyPrefix);
  }

  @Override
  public void removeByKeyPrefix(ContextID contextID, ContextType contextType, String keyPrefix) {
    contextMapMapper.removeByKeyPrefixAndContextType(contextID, contextType, keyPrefix);
  }

  @Override
  public void removeByKey(ContextID contextID, ContextType contextType, String keyStr) {
    contextMapMapper.removeByKeyAndContextType(contextID, contextType, keyStr);
  }

  @Override
  public List<ContextKeyValue> searchContextIDByTime(
      Date createTimeStart,
      Date createTimeEnd,
      Date updateTimeStart,
      Date updateTimeEnd,
      Date accessTimeStart,
      Date accessTimeEnd) {
    List<PersistenceContextKeyValue> result =
        contextMapMapper.getAllContextMapByTime(
            createTimeStart,
            createTimeEnd,
            updateTimeStart,
            updateTimeEnd,
            accessTimeStart,
            accessTimeEnd);
    List<ContextKeyValue> rsList = new ArrayList<>();
    if (null != result) {
      for (PersistenceContextKeyValue pKV : result) {
        rsList.add(pKV);
      }
    }
    return rsList;
  }
}
