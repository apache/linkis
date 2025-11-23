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

import org.apache.linkis.cs.common.entity.source.CommonContextID;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.persistence.entity.PersistenceContextIDListener;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ContextIDListenerMapperTest extends BaseDaoTest {

  @Autowired private ContextIDListenerMapper contextIDListenerMapper;

  private PersistenceContextIDListener createPersistenceContextIDListener() {

    PersistenceContextIDListener persistenceContextIDListener = new PersistenceContextIDListener();
    persistenceContextIDListener.setAccessTime(new Date());
    persistenceContextIDListener.setContextId("1");
    persistenceContextIDListener.setCreateTime(new Date());
    persistenceContextIDListener.setId(3);
    persistenceContextIDListener.setUpdateTime(new Date());
    persistenceContextIDListener.setSource("source");
    return persistenceContextIDListener;
  }

  @Test
  @DisplayName("createIDListenerTest")
  public void createIDListenerTest() {

    ContextID contextID = new CommonContextID();
    contextID.setContextId("1");

    PersistenceContextIDListener listener = createPersistenceContextIDListener();
    contextIDListenerMapper.createIDListener(listener);

    List<PersistenceContextIDListener> all = contextIDListenerMapper.getAll(contextID);
    Assertions.assertTrue(all.size() > 0);
  }

  @Test
  @DisplayName("removeTest")
  public void removeTest() {

    ContextID contextID = new CommonContextID();
    contextID.setContextId("1");

    PersistenceContextIDListener listener = createPersistenceContextIDListener();
    contextIDListenerMapper.createIDListener(listener);
    contextIDListenerMapper.remove(listener);
    List<PersistenceContextIDListener> all = contextIDListenerMapper.getAll(contextID);
    Assertions.assertTrue(all.size() == 0);
  }

  @Test
  @DisplayName("removeAllTest")
  public void removeAllTest() {

    ContextID contextID = new CommonContextID();
    contextID.setContextId("1");

    PersistenceContextIDListener listener = createPersistenceContextIDListener();
    contextIDListenerMapper.createIDListener(listener);
    contextIDListenerMapper.removeAll(contextID);
    List<PersistenceContextIDListener> all = contextIDListenerMapper.getAll(contextID);
    Assertions.assertTrue(all.size() == 0);
  }
}
