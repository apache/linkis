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

package org.apache.linkis.cs.persistence;

import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.history.ContextHistory;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.persistence.entity.PersistenceContextID;
import org.apache.linkis.cs.persistence.persistence.ContextHistoryPersistence;
import org.apache.linkis.cs.persistence.persistence.KeywordContextHistoryPersistence;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ContextHistoryTest {
  AnnotationConfigApplicationContext context = null;
  ContextHistoryPersistence contextHistoryPersistence = null;

  KeywordContextHistoryPersistence keywordContextHistoryPersistence = null;

  public void before() {
    context = new AnnotationConfigApplicationContext(Scan.class);
    contextHistoryPersistence = context.getBean(ContextHistoryPersistence.class);
    keywordContextHistoryPersistence = context.getBean(KeywordContextHistoryPersistence.class);
  }

  public void testcreateContextHistory() throws CSErrorException, JsonProcessingException {
    AContextHistory aContextHistory = new AContextHistory();
    PersistenceContextID persistenceContextID = new PersistenceContextID();
    persistenceContextID.setContextId(String.valueOf(new Random().nextInt(100000)));
    aContextHistory.setHistoryJson("json");
    aContextHistory.setContextType(ContextType.DATA);
    aContextHistory.setId(new Random().nextInt(100000));
    aContextHistory.setKeyword("keywords");
    aContextHistory.setSource("source");
    contextHistoryPersistence.createHistory(persistenceContextID, aContextHistory);
  }

  public void testDeleteContextHistory() throws CSErrorException {
    // contextIDPersistence.deleteContextID(-1193959466);
  }

  public void testGetContextHistory() throws CSErrorException {
    AContextID aContextID = new AContextID();
    aContextID.setContextId("53277");
    ContextHistory history = contextHistoryPersistence.getHistory(aContextID, 19359L);
    AContextHistory history1 = (AContextHistory) history;
    System.out.println(history1.getVersion());
  }

  public void testGetContextHistoryBySource() throws CSErrorException {
    AContextID aContextID = new AContextID();
    aContextID.setContextId("53277");
    ContextHistory history = contextHistoryPersistence.getHistory(aContextID, "source");
    AContextHistory history1 = (AContextHistory) history;
    System.out.println(history1.getVersion());
  }

  public void testGetHistoriesByContextID() throws CSErrorException {
    AContextID aContextID = new AContextID();
    aContextID.setContextId("53277");
    List<ContextHistory> histories = contextHistoryPersistence.getHistories(aContextID);
    System.out.println(histories.size());
  }

  public void testSearchHistoriesByContextIDAndKeywords() throws CSErrorException {
    AContextID aContextID = new AContextID();
    aContextID.setContextId("53277");
    List<ContextHistory> histories =
        keywordContextHistoryPersistence.search(aContextID, new String[] {"keyword1", "keyword2"});
    System.out.println(histories.size());
  }

  public void testSearchHistoriesByType() throws CSErrorException {
    List<ContextHistory> histories =
        keywordContextHistoryPersistence.search(
            ContextType.METADATA, new String[] {"keyword1", "keyword2"});
    System.out.println(histories.size());
  }

  public void testRemoveContextHistory() throws CSErrorException {
    AContextID aContextID = new AContextID();
    aContextID.setContextId("5327745");
    contextHistoryPersistence.removeHistory(aContextID, "source");
  }

  public void testUpdateContextHistory() throws CSErrorException {
    AContextHistory aContextHistory = new AContextHistory();
    aContextHistory.setKeyword("update keywords");
    aContextHistory.setContextType(ContextType.METADATA);
    aContextHistory.setSource("updatesource");
    aContextHistory.setId(19359);
    AContextID aContextID = new AContextID();
    contextHistoryPersistence.updateHistory(aContextID, aContextHistory);
  }
}
