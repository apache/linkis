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

import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.persistence.conf.PersistenceConf;
import org.apache.linkis.cs.persistence.dao.ContextIDMapper;
import org.apache.linkis.cs.persistence.entity.ExtraFieldClass;
import org.apache.linkis.cs.persistence.entity.PersistenceContextID;
import org.apache.linkis.cs.persistence.persistence.ContextIDPersistence;
import org.apache.linkis.cs.persistence.util.PersistenceUtils;
import org.apache.linkis.server.BDPJettyServerHelper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ContextIDPersistenceImpl implements ContextIDPersistence {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired private ContextIDMapper contextIDMapper;

  private Class<PersistenceContextID> pClass = PersistenceContextID.class;

  private ObjectMapper json = BDPJettyServerHelper.jacksonJson();

  @Override
  public ContextID createContextID(ContextID contextID) throws CSErrorException {
    try {
      Pair<PersistenceContextID, ExtraFieldClass> pContextID =
          PersistenceUtils.transfer(contextID, pClass);
      pContextID.getFirst().setSource(json.writeValueAsString(pContextID.getSecond()));
      Date now = new Date();
      pContextID.getFirst().setCreateTime(now);
      pContextID.getFirst().setUpdateTime(now);
      pContextID.getFirst().setAccessTime(now);
      contextIDMapper.createContextID(pContextID.getFirst());
      contextID.setContextId(pContextID.getFirst().getContextId());
      return contextID;
    } catch (JsonProcessingException e) {
      logger.error("writeAsJson failed:", e);
      throw new CSErrorException(97000, e.getMessage());
    }
  }

  @Override
  public void deleteContextID(String contextId) {
    contextIDMapper.deleteContextID(contextId);
  }

  @Override
  public void updateContextID(ContextID contextID) throws CSErrorException {
    // contextId和source没有设置更新点
    Pair<PersistenceContextID, ExtraFieldClass> pContextID =
        PersistenceUtils.transfer(contextID, pClass);
    if (null == pContextID.getFirst().getAccessTime()) {
      pContextID.getFirst().setUpdateTime(new Date());
    }
    contextIDMapper.updateContextID(pContextID.getFirst());
  }

  @Override
  public ContextID getContextID(String contextId) throws CSErrorException {
    try {
      PersistenceContextID pContextID = contextIDMapper.getContextID(contextId);
      if (pContextID == null) return null;
      if (PersistenceConf.ENABLE_CS_DESERIALIZE_REPLACE_PACKAGE_HEADER.getValue()) {
        if (StringUtils.isBlank(pContextID.getSource())
            || StringUtils.isBlank(
                PersistenceConf.CS_DESERIALIZE_REPLACE_PACKAGE_HEADER.getValue())) {
          logger.error(
              "Source : {} of ContextID or CSID_REPLACE_PACKAGE_HEADER : {} cannot be empty.",
              pContextID.getSource(),
              PersistenceConf.CS_DESERIALIZE_REPLACE_PACKAGE_HEADER.getValue());
        } else {
          if (pContextID
              .getSource()
              .contains(PersistenceConf.CS_DESERIALIZE_REPLACE_PACKAGE_HEADER.getValue())) {
            if (logger.isDebugEnabled()) {
              logger.debug(
                  "Will replace package header of source : {} from : {} to : {}",
                  pContextID.getSource(),
                  PersistenceConf.CS_DESERIALIZE_REPLACE_PACKAGE_HEADER.getValue(),
                  PersistenceConf.CSID_PACKAGE_HEADER);
            }
            pContextID.setSource(
                pContextID
                    .getSource()
                    .replaceAll(
                        PersistenceConf.CS_DESERIALIZE_REPLACE_PACKAGE_HEADER.getValue(),
                        PersistenceConf.CSID_PACKAGE_HEADER));
          }
        }
      }
      ExtraFieldClass extraFieldClass =
          json.readValue(pContextID.getSource(), ExtraFieldClass.class);
      ContextID contextID = PersistenceUtils.transfer(extraFieldClass, pContextID);
      return contextID;
    } catch (IOException e) {
      logger.error("readJson failed:", e);
      throw new CSErrorException(97000, e.getMessage());
    }
  }

  @Override
  public List<PersistenceContextID> searchContextID(PersistenceContextID contextID)
      throws CSErrorException {
    PersistenceContextID persistenceContextID = new PersistenceContextID();
    persistenceContextID.setContextId(contextID.getContextId());
    persistenceContextID.setContextIDType(contextID.getContextIDType());
    return contextIDMapper.searchContextID(persistenceContextID);
  }

  @Override
  public List<PersistenceContextID> searchCSIDByTime(
      Date createTimeStart,
      Date createTimeEnd,
      Date updateTimeStart,
      Date updateTimeEnd,
      Date accessTimeStart,
      Date accessTimeEnd) {
    return contextIDMapper.getAllContextIDByTime(
        createTimeStart,
        createTimeEnd,
        updateTimeStart,
        updateTimeEnd,
        accessTimeStart,
        accessTimeEnd);
  }
}
