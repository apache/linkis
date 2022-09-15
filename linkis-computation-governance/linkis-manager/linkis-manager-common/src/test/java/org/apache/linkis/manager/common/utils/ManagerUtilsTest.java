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

package org.apache.linkis.manager.common.utils;

import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.label.entity.Label;

import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** ManagerUtils Tester */
public class ManagerUtilsTest {

  @BeforeEach
  @DisplayName("Each unit test method is executed once before execution")
  public void before() throws Exception {}

  @AfterEach
  @DisplayName("Each unit test method is executed once before execution")
  public void after() throws Exception {}

  @Test
  public void testGetValueOrDefault() throws Exception {
    String key = "testKey";
    String value = "testValue";
    HashMap<String, Object> map = new HashMap<>();
    map.put(key, value);
    String defaultVal = ManagerUtils.getValueOrDefault(map, key, null);
    assertEquals(value, defaultVal);

    defaultVal = ManagerUtils.getValueOrDefault(map, "otherKey", "test");
    assertEquals("test", defaultVal);
  }

  @Test
  public void testGetAdminUser() throws Exception {
    String adminUser = ManagerUtils.getAdminUser();
    assertEquals("hadoop", adminUser);
  }

  @Test
  public void testPersistenceLabelToRealLabel() throws Exception {
    PersistenceLabel pl = new PersistenceLabel();
    pl.setLabelKey("testKey");

    HashMap<String, String> map = new HashMap<>();
    map.put("test", "test");
    pl.setValue(map);
    Label<?> label = ManagerUtils.persistenceLabelToRealLabel(pl);
    assertNull(label);
  }
}
