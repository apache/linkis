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

import org.apache.linkis.cs.persistence.entity.PersistenceContextID;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ContextIDMapperTest extends BaseDaoTest {

  @Autowired private ContextIDMapper contextIDMapper;

  private PersistenceContextID createPersistenceContextID() {

    PersistenceContextID persistenceContextID = new PersistenceContextID();
    persistenceContextID.setAccessTime(new Date());
    persistenceContextID.setCreateTime(new Date());
    persistenceContextID.setUpdateTime(new Date());
    persistenceContextID.setApplication("application-1");
    persistenceContextID.setBackupInstance("1");
    persistenceContextID.setExpireTime(new Date());
    persistenceContextID.setSource("source");
    persistenceContextID.setUser("hadoop");
    persistenceContextID.setInstance("1");
    return persistenceContextID;
  }

  @Test
  @DisplayName("createContextIDTest")
  public void createContextIDTest() {

    PersistenceContextID contextID = createPersistenceContextID();
    contextIDMapper.createContextID(contextID);
    PersistenceContextID persistenceContextID =
        contextIDMapper.getContextID(contextID.getContextId());
    Assertions.assertNotNull(persistenceContextID);
  }

  @Test
  @DisplayName("deleteContextIDTest")
  public void deleteContextIDTest() {

    PersistenceContextID contextID = createPersistenceContextID();
    contextIDMapper.createContextID(contextID);
    contextIDMapper.deleteContextID(contextID.getContextId());
    PersistenceContextID persistenceContextID =
        contextIDMapper.getContextID(contextID.getContextId());
    Assertions.assertNull(persistenceContextID);
  }

  @Test
  @DisplayName("updateContextIDTest")
  public void updateContextIDTest() {
    String newName = "hadoops";
    PersistenceContextID contextID = createPersistenceContextID();
    contextIDMapper.createContextID(contextID);
    contextID.setUser(newName);
    contextIDMapper.updateContextID(contextID);
    PersistenceContextID persistenceContextID =
        contextIDMapper.getContextID(contextID.getContextId());
    Assertions.assertEquals(newName, persistenceContextID.getUser());
  }

  @Test
  @DisplayName("searchContextIDTest")
  public void searchContextIDTest() {

    PersistenceContextID contextID = createPersistenceContextID();
    contextIDMapper.createContextID(contextID);
    List<PersistenceContextID> contextIDS = contextIDMapper.searchContextID(contextID);
    Assertions.assertNotNull(contextIDS);
  }

  @Test
  @DisplayName("getAllContextIDByTimeTest")
  public void getAllContextIDByTimeTest() {

    PersistenceContextID contextID = createPersistenceContextID();
    contextIDMapper.createContextID(contextID);
    List<PersistenceContextID> contextIDS =
        contextIDMapper.getAllContextIDByTime(null, null, null, null, null, null);
    Assertions.assertTrue(contextIDS.size() > 0);
  }
}
