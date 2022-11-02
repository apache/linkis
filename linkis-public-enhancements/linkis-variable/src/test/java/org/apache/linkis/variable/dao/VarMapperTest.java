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

package org.apache.linkis.variable.dao;

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

import org.apache.linkis.variable.entity.VarKey;
import org.apache.linkis.variable.entity.VarKeyUser;

import org.springframework.beans.factory.annotation.Autowired;

import org.h2.tools.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VarMapperTest extends BaseDaoTest {
  @Autowired private VarMapper varMapper;

  private VarKey insertVarKey() {
    VarKey varKey = new VarKey();
    varKey.setKey("myWork6");
    varKey.setApplicationID(-1L);
    varMapper.insertKey(varKey);
    return varKey;
  }

  private VarKeyUser insertVarKeyUser() {
    VarKeyUser varKeyUser = new VarKeyUser();
    varKeyUser.setApplicationID(-1L);
    varKeyUser.setUserName("tom6");
    varKeyUser.setValue("sing");
    varKeyUser.setKeyID(6L);
    varMapper.insertValue(varKeyUser);
    return varKeyUser;
  }

  @BeforeAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void beforeAll() throws Exception {
    // Start the console of h2 to facilitate viewing of h2 data
    Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
  }

  @AfterAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void afterAll() throws Exception {}

  @Test
  void testListGlobalVariable() {
    // h2数据库执行生成的sql语句报错,在mysql数据库中可以正常运行
    //        List<VarKeyValueVO> varKeyValueVOList = varMapper.listGlobalVariable("tom1");
    //        assertEquals(1,varKeyValueVOList.size());
  }

  @Test
  void testGetValueByKeyID() {
    VarKeyUser varKeyUser = varMapper.getValueByKeyID(3L);
    assertEquals("tom3", varKeyUser.getUserName());
  }

  @Test
  void testRemoveKey() {
    varMapper.removeKey(1L);
  }

  @Test
  void testRemoveValue() {
    varMapper.removeValue(1L);
    assertEquals(null, varMapper.getValueByKeyID(1L));
  }

  @Test
  void testInsertKey() {
    VarKey varKey = insertVarKey();
    assertTrue(varKey.getId() > 0);
  }

  @Test
  void testInsertValue() {
    VarKeyUser varKeyUser = insertVarKeyUser();
    assertTrue(varKeyUser.getId() > 0);
  }

  @Test
  void testUpdateValue() {
    varMapper.updateValue(5L, "sing too");
    assertEquals("sing too", varMapper.getValueByKeyID(5L).getValue());
  }
}
