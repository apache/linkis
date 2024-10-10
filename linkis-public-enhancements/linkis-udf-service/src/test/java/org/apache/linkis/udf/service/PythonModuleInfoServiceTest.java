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

package org.apache.linkis.udf.service;

import org.apache.linkis.udf.dao.PythonModuleInfoMapper;
import org.apache.linkis.udf.entity.PythonModuleInfo;
import org.apache.linkis.udf.service.impl.PythonModuleInfoServiceImpl;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/** PythonModuleInfoServiceImplTest 类用于对 PythonModuleInfoServiceImpl 进行单元测试。 */
public class PythonModuleInfoServiceTest {

  @Mock private PythonModuleInfoMapper pythonModuleInfoMapper;

  @InjectMocks private PythonModuleInfoServiceImpl pythonModuleInfoServiceImpl;

  /** 在每个测试方法执行前执行，用于初始化测试环境。 */
  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  /** 测试 getByConditions 方法的功能。 */
  @Test
  public void testGetByConditions() {
    PythonModuleInfo mockInfo = new PythonModuleInfo();
    mockInfo.setId(1L);
    mockInfo.setName("TestModule");
    when(pythonModuleInfoMapper.selectByConditions(mockInfo)).thenReturn(Arrays.asList(mockInfo));

    List<PythonModuleInfo> result = pythonModuleInfoServiceImpl.getByConditions(mockInfo);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(mockInfo.getId(), result.get(0).getId());
    assertEquals(mockInfo.getName(), result.get(0).getName());
  }

  /** 测试 updatePythonModuleInfo 方法的功能。 */
  @Test
  public void testUpdatePythonModuleInfo() {
    PythonModuleInfo mockInfo = new PythonModuleInfo();
    mockInfo.setId(1L);
    mockInfo.setName("UpdatedModule");
    when(pythonModuleInfoMapper.updatePythonModuleInfo(mockInfo)).thenReturn(1);

    int result = pythonModuleInfoServiceImpl.updatePythonModuleInfo(mockInfo);

    assertEquals(1, result);
  }

  /** 测试 insertPythonModuleInfo 方法的功能。 */
  @Test
  public void testInsertPythonModuleInfo() {
    PythonModuleInfo mockInfo = new PythonModuleInfo();
    mockInfo.setId(1L);
    mockInfo.setName("NewModule");
    when(pythonModuleInfoMapper.insertPythonModuleInfo(mockInfo)).thenReturn(1L);

    Long result = pythonModuleInfoServiceImpl.insertPythonModuleInfo(mockInfo);

    assertNotNull(result);
    assertEquals(1L, result.longValue());
  }

  /** 测试 getByUserAndNameAndId 方法的功能。 */
  @Test
  public void testGetByUserAndNameAndId() {
    PythonModuleInfo mockInfo = new PythonModuleInfo();
    mockInfo.setId(1L);
    mockInfo.setName("UniqueModule");
    when(pythonModuleInfoMapper.selectByUserAndNameAndId(mockInfo)).thenReturn(mockInfo);

    PythonModuleInfo result = pythonModuleInfoServiceImpl.getByUserAndNameAndId(mockInfo);

    assertNotNull(result);
    assertEquals(mockInfo.getId(), result.getId());
    assertEquals(mockInfo.getName(), result.getName());
  }

  /** 测试 getPathsByUsernameAndEnginetypes 方法的功能。 */
  @Test
  public void testGetPathsByUsernameAndEnginetypes() {
    String username = "testUser";
    List<String> enginetypes = Arrays.asList("Engine1", "Engine2");
    PythonModuleInfo mockInfo1 = new PythonModuleInfo();
    mockInfo1.setId(1L);
    mockInfo1.setName("Module1");
    PythonModuleInfo mockInfo2 = new PythonModuleInfo();
    mockInfo2.setId(2L);
    mockInfo2.setName("Module2");
    when(pythonModuleInfoMapper.selectPathsByUsernameAndEnginetypes(username, enginetypes))
        .thenReturn(Arrays.asList(mockInfo1, mockInfo2));

    List<PythonModuleInfo> result =
        pythonModuleInfoServiceImpl.getPathsByUsernameAndEnginetypes(username, enginetypes);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.contains(mockInfo1));
    assertTrue(result.contains(mockInfo2));
  }
}
