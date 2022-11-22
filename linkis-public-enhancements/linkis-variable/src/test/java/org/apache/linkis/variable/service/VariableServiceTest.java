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

package org.apache.linkis.variable.service;

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

import org.apache.linkis.protocol.variable.ResponseQueryVariable;
import org.apache.linkis.variable.dao.VarMapper;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class VariableServiceTest {
  @InjectMocks VariableServiceImpl variableService;

  @Mock VarMapper varMapper;

  @Test
  void testQueryGolbalVariable() {
    Mockito.when(varMapper.listGlobalVariable("tom1")).thenReturn(new ArrayList<>());
    assertEquals(
        new ResponseQueryVariable().getClass(),
        variableService.queryGolbalVariable("tom1").getClass());
  }

  @Test
  void testQueryAppVariable() {
    Mockito.when(varMapper.listGlobalVariable("tom1")).thenReturn(new ArrayList<>());
    assertEquals(
        new ResponseQueryVariable().getClass(),
        variableService.queryAppVariable("tom1", "bob", "link").getClass());
  }

  @Test
  void testListGlobalVariable() {
    Mockito.when(varMapper.listGlobalVariable("tom1")).thenReturn(new ArrayList<>());
    assertEquals(
        new ArrayList<>().getClass(), variableService.listGlobalVariable("tom1").getClass());
  }

  @Test
  void testSaveGlobalVaraibles() {
    //        Mockito.when(varMapper.listGlobalVariable("tom1")).thenReturn(new ArrayList<>());
    //        variableService.saveGlobalVaraibles(new ArrayList<>(),new ArrayList<>(),"tom");
  }
}
