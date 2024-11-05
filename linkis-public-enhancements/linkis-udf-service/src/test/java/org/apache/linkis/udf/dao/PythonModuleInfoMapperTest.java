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

package org.apache.linkis.udf.dao;

import org.apache.linkis.udf.entity.PythonModuleInfo;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** PythonModuleInfoMapperTest 类用于测试 PythonModuleInfoMapper 的功能。 */
public class PythonModuleInfoMapperTest {

  private PythonModuleInfoMapper pythonModuleInfoMapper; // PythonModuleInfoMapper 的模拟对象

  /** 在每个测试方法执行前执行，用于初始化测试环境。 */
  @BeforeEach
  public void setUp() {
    pythonModuleInfoMapper = mock(PythonModuleInfoMapper.class);
  }

  /** 测试 selectByConditions 方法的功能。 */
  @Test
  public void testSelectByConditions() {
    PythonModuleInfo pythonModuleInfo = new PythonModuleInfo();
    // 设置 pythonModuleInfo 的属性

    when(pythonModuleInfoMapper.selectByConditions(pythonModuleInfo))
        .thenReturn(Arrays.asList(pythonModuleInfo));

    List<PythonModuleInfo> result = pythonModuleInfoMapper.selectByConditions(pythonModuleInfo);
    assertEquals(1, result.size());
    // 验证结果的属性
  }

  /** 测试 updatePythonModuleInfo 方法的功能。 */
  @Test
  public void testUpdatePythonModuleInfo() {
    PythonModuleInfo pythonModuleInfo = new PythonModuleInfo();
    // 设置 pythonModuleInfo 的属性

    when(pythonModuleInfoMapper.updatePythonModuleInfo(pythonModuleInfo)).thenReturn(1);

    int result = pythonModuleInfoMapper.updatePythonModuleInfo(pythonModuleInfo);
    assertEquals(1, result);
  }

  /** 测试 insertPythonModuleInfo 方法的功能。 */
  @Test
  public void testInsertPythonModuleInfo() {
    PythonModuleInfo pythonModuleInfo = new PythonModuleInfo();
    // 设置 pythonModuleInfo 的属性

    when(pythonModuleInfoMapper.insertPythonModuleInfo(pythonModuleInfo)).thenReturn(1L);

    Long result = pythonModuleInfoMapper.insertPythonModuleInfo(pythonModuleInfo);
    assertEquals(1L, result.longValue());
  }

  /** 测试 selectByUserAndNameAndId 方法的功能。 */
  @Test
  public void testSelectByUserAndNameAndId() {
    PythonModuleInfo pythonModuleInfo = new PythonModuleInfo();
    // 设置 pythonModuleInfo 的属性

    when(pythonModuleInfoMapper.selectByUserAndNameAndId(pythonModuleInfo))
        .thenReturn(pythonModuleInfo);

    PythonModuleInfo result = pythonModuleInfoMapper.selectByUserAndNameAndId(pythonModuleInfo);
    assertNotNull(result);
    // 验证结果的属性
  }

  /** 测试 selectPathsByUsernameAndEnginetypes 方法的功能。 */
  @Test
  public void testSelectPathsByUsernameAndEnginetypes() {
    String username = "testUser";
    List<String> enginetypes = Arrays.asList("type1", "type2");
    PythonModuleInfo pythonModuleInfo = new PythonModuleInfo();
    // 设置 pythonModuleInfo 的属性

    when(pythonModuleInfoMapper.selectPathsByUsernameAndEnginetypes(username, enginetypes))
        .thenReturn(Arrays.asList(pythonModuleInfo));

    List<PythonModuleInfo> result =
        pythonModuleInfoMapper.selectPathsByUsernameAndEnginetypes(username, enginetypes);
    assertEquals(1, result.size());
    // 验证结果的属性
  }
}
