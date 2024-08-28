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

import org.apache.linkis.cs.common.entity.listener.CommonContextKeyListenerDomain;
import org.apache.linkis.cs.common.entity.listener.ContextKeyListenerDomain;
import org.apache.linkis.cs.persistence.entity.PersistenceContextKeyListener;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ContextKeyListenerMapperTest extends BaseDaoTest {

  @Autowired private ContextKeyListenerMapper contextKeyListenerMapper;

  private PersistenceContextKeyListener createPersistenceContextKeyListener() {

    PersistenceContextKeyListener persistenceContextKeyListener =
        new PersistenceContextKeyListener();
    persistenceContextKeyListener.setCreateTime(new Date());
    persistenceContextKeyListener.setUpdateTime(new Date());
    persistenceContextKeyListener.setKeyId(1);
    persistenceContextKeyListener.setId(1);
    persistenceContextKeyListener.setSource("source");

    return persistenceContextKeyListener;
  }

  @Test
  @DisplayName("createKeyListenerTest")
  public void createKeyListenerTest() {
    List<Integer> keyIds = new ArrayList<>();
    keyIds.add(1);
    PersistenceContextKeyListener listener = createPersistenceContextKeyListener();
    contextKeyListenerMapper.createKeyListener(listener);
    List<PersistenceContextKeyListener> all = contextKeyListenerMapper.getAll(keyIds);
    Assertions.assertTrue(all.size() > 0);
  }

  @Test
  @DisplayName("removeTest")
  public void removeTest() {
    List<Integer> keyIds = new ArrayList<>();
    keyIds.add(1);

    ContextKeyListenerDomain contextKeyListenerDomain = new CommonContextKeyListenerDomain();
    contextKeyListenerDomain.setSource("source");

    PersistenceContextKeyListener listener = createPersistenceContextKeyListener();
    contextKeyListenerMapper.createKeyListener(listener);

    contextKeyListenerMapper.remove(contextKeyListenerDomain, 1);
    List<PersistenceContextKeyListener> all = contextKeyListenerMapper.getAll(keyIds);
    Assertions.assertTrue(all.size() == 0);
  }

  @Test
  @DisplayName("removeAllTest")
  public void removeAllTest() {

    List<Integer> keyIds = new ArrayList<>();
    keyIds.add(1);

    ContextKeyListenerDomain contextKeyListenerDomain = new CommonContextKeyListenerDomain();
    contextKeyListenerDomain.setSource("source");

    PersistenceContextKeyListener listener = createPersistenceContextKeyListener();
    contextKeyListenerMapper.createKeyListener(listener);

    contextKeyListenerMapper.removeAll(keyIds);
    List<PersistenceContextKeyListener> all = contextKeyListenerMapper.getAll(keyIds);
    Assertions.assertTrue(all.size() == 0);
  }

  @Test
  @DisplayName("getAllTest")
  public void getAllTest() {

    List<Integer> keyIds = new ArrayList<>();
    keyIds.add(1);

    ContextKeyListenerDomain contextKeyListenerDomain = new CommonContextKeyListenerDomain();
    contextKeyListenerDomain.setSource("source");

    PersistenceContextKeyListener listener = createPersistenceContextKeyListener();
    contextKeyListenerMapper.createKeyListener(listener);
    List<PersistenceContextKeyListener> all = contextKeyListenerMapper.getAll(keyIds);
    Assertions.assertTrue(all.size() > 0);
  }
}
