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

package org.apache.linkis.cs.persistence.dao;

import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.CommonContextID;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.persistence.entity.PersistenceContextHistory;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextHistoryMapperTest extends BaseDaoTest {

  private static final Logger LOG = LoggerFactory.getLogger(ContextHistoryMapperTest.class);

  @Autowired private ContextHistoryMapper contextHistoryMapper;

  private PersistenceContextHistory createPersistenceContextHistory() {
    PersistenceContextHistory history = new PersistenceContextHistory();
    history.setAccessTime(new Date());
    history.setContextId("1");
    history.setContextType(ContextType.UDF);
    history.setCreateTime(new Date());
    history.setId(3);
    history.setUpdateTime(new Date());
    history.setKeyword("success");
    history.setHistoryJson("{}");
    history.setSource("source");
    return history;
  }

  @Test
  @DisplayName("createHistoryTest")
  public void createHistoryTest() {
    ContextID contextID = new CommonContextID();
    contextID.setContextId("1");
    PersistenceContextHistory history = createPersistenceContextHistory();
    contextHistoryMapper.createHistory(history);
    PersistenceContextHistory contextHistory =
        contextHistoryMapper.getHistory(contextID, Long.valueOf(String.valueOf(history.getId())));
    Assertions.assertNotNull(contextHistory);
  }

  @Test
  @DisplayName("getHistoryBySourceTest")
  public void getHistoryBySourceTest() {

    ContextID contextID = new CommonContextID();
    contextID.setContextId("1");
    PersistenceContextHistory history = createPersistenceContextHistory();
    contextHistoryMapper.createHistory(history);
    PersistenceContextHistory contextHistory =
        contextHistoryMapper.getHistoryBySource(contextID, "source");
    Assertions.assertNotNull(contextHistory);
  }

  @Test
  @DisplayName("getHistoriesByContextIDTest")
  public void getHistoriesByContextIDTest() {

    ContextID contextID = new CommonContextID();
    contextID.setContextId("1");

    PersistenceContextHistory history = createPersistenceContextHistory();
    contextHistoryMapper.createHistory(history);

    List<PersistenceContextHistory> histories =
        contextHistoryMapper.getHistoriesByContextID(contextID);
    Assertions.assertTrue(histories.size() > 0);
  }

  @Test
  @DisplayName("removeHistoryTest")
  public void removeHistoryTest() {

    ContextID contextID = new CommonContextID();
    contextID.setContextId("1");

    PersistenceContextHistory history = createPersistenceContextHistory();
    contextHistoryMapper.createHistory(history);
    contextHistoryMapper.removeHistory(contextID, "source");

    List<PersistenceContextHistory> histories =
        contextHistoryMapper.getHistoriesByContextID(contextID);
    Assertions.assertTrue(histories.size() == 0);
  }

  @Test
  @DisplayName("updateHistoryTest")
  public void updateHistoryTest() {
    String keyWord = "Test it.";
    ContextID contextID = new CommonContextID();
    contextID.setContextId("1");

    PersistenceContextHistory history = createPersistenceContextHistory();
    contextHistoryMapper.createHistory(history);
    history.setKeyword(keyWord);
    contextHistoryMapper.updateHistory(contextID, history);

    PersistenceContextHistory contextHistory =
        contextHistoryMapper.getHistory(contextID, Long.valueOf(String.valueOf(history.getId())));
    Assertions.assertEquals(keyWord, contextHistory.getKeyword());
  }

  @Test
  @DisplayName("searchByKeywordsTest")
  public void searchByKeywordsTest() {
    String[] keyWords = {"success"};

    ContextID contextID = new CommonContextID();
    contextID.setContextId("1");

    PersistenceContextHistory history = createPersistenceContextHistory();
    contextHistoryMapper.createHistory(history);

    List<PersistenceContextHistory> histories =
        contextHistoryMapper.searchByKeywords(contextID, keyWords);
    Assertions.assertTrue(histories.size() > 0);
  }

  @Test
  @DisplayName("searchByKeywordsAndTypeTest")
  public void searchByKeywordsAndTypeTest() {
    String[] keyWords = {"success"};

    ContextID contextID = new CommonContextID();
    contextID.setContextId("1");

    PersistenceContextHistory history = createPersistenceContextHistory();
    contextHistoryMapper.createHistory(history);
    List<PersistenceContextHistory> histories =
        contextHistoryMapper.searchByKeywordsAndType(ContextType.UDF, keyWords);
    Assertions.assertTrue(histories.size() > 0);
  }
}
