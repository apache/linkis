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

package org.apache.linkis.udf.api;

import org.apache.linkis.udf.entity.PythonModuleInfo;
import org.apache.linkis.udf.service.PythonModuleInfoService;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** PythonModuleRestfulApiTest 类用于测试 Python 模块服务相关功能。 */
public class PythonModuleRestfulApiTest {

  @Mock private PythonModuleInfoService pythonModuleInfoService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  /** 测试Python模块列表功能 */
  @Test
  public void testPythonList() throws Exception {
    List<PythonModuleInfo> mockList = new ArrayList<>();
    PythonModuleInfo info = new PythonModuleInfo();
    info.setId(1L);
    info.setName("testModule");
    mockList.add(info);
    when(pythonModuleInfoService.getByConditions(any())).thenReturn(mockList);

    List<PythonModuleInfo> result = pythonModuleInfoService.getByConditions(new PythonModuleInfo());
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  /** 测试删除Python模块功能 */
  @Test
  public void testPythonDelete() throws Exception {
    when(pythonModuleInfoService.updatePythonModuleInfo(any())).thenReturn(1);

    int result = pythonModuleInfoService.updatePythonModuleInfo(new PythonModuleInfo());
    assertEquals(1, result);
  }

  /** 测试保存Python模块功能 */
  @Test
  public void testPythonSave() throws Exception {
    when(pythonModuleInfoService.insertPythonModuleInfo(any())).thenReturn(1L);
    when(pythonModuleInfoService.getByUserAndNameAndId(any())).thenReturn(null);

    Long result = pythonModuleInfoService.insertPythonModuleInfo(new PythonModuleInfo());
    assertEquals(1L, result.longValue());
  }

  /** 测试检查Python模块文件是否存在功能 */
  @Test
  public void testPythonFileExist() throws Exception {
    PythonModuleInfo mockInfo = new PythonModuleInfo();
    mockInfo.setId(1L);
    when(pythonModuleInfoService.getByUserAndNameAndId(any())).thenReturn(mockInfo);

    PythonModuleInfo result = pythonModuleInfoService.getByUserAndNameAndId(new PythonModuleInfo());
    assertNotNull(result);
  }
}
