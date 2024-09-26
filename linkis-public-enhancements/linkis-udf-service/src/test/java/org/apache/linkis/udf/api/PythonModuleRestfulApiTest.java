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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** PythonModuleRestfulApiTest 类用于对 PythonModuleRestfulApi 进行单元测试。 */
public class PythonModuleRestfulApiTest {
  @Autowired protected MockMvc mockMvc;
  /** 测试Python模块列表功能 */
  @Test
  public void testPythonList() throws Exception {
    // 测试获取Python模块列表
    mockMvc
        .perform(
            get("/python-list")
                .param("name", "testModule")
                .param("engineType", "spark")
                .param("username", "testUser")
                .param("isLoad", "0")
                .param("isExpire", "1")
                .param("pageNow", "1")
                .param("pageSize", "10"))
        .andExpect(status().isOk());

    // 测试获取Python模块列表（无参数）
    mockMvc.perform(get("/python-list")).andExpect(status().isOk());

    // 测试获取Python模块列表（空参数）
    mockMvc
        .perform(
            get("/python-list")
                .param("name", "")
                .param("engineType", "")
                .param("username", "")
                .param("isLoad", "")
                .param("isExpire", "")
                .param("pageNow", "")
                .param("pageSize", ""))
        .andExpect(status().isOk());
  }

  /** 测试删除Python模块功能 */
  @Test
  public void testPythonDelete() throws Exception {
    // 测试删除Python模块
    mockMvc
        .perform(get("/python-delete").param("id", "1").param("isExpire", "0"))
        .andExpect(status().isOk());

    // 测试删除不存在的Python模块
    mockMvc
        .perform(get("/python-delete").param("id", "999").param("isExpire", "0"))
        .andExpect(status().isNotFound());

    // 测试删除Python模块时传入无效参数
    mockMvc
        .perform(get("/python-delete").param("id", "1").param("isExpire", "2"))
        .andExpect(status().isBadRequest());
  }

  /** 测试保存Python模块功能 */
  @Test
  public void testPythonSave() throws Exception {
    // 测试保存Python模块
    mockMvc
        .perform(
            post("/python-save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"testModule\",\"path\":\"/path/to/module.py\",\"engineType\":\"python\",\"isLoad\":1,\"isExpire\":0}"))
        .andExpect(status().isOk());

    // 测试保存Python模块时传入空名称
    mockMvc
        .perform(
            post("/python-save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"\",\"path\":\"/path/to/module.py\",\"engineType\":\"python\",\"isLoad\":1,\"isExpire\":0}"))
        .andExpect(status().isBadRequest());

    // 测试保存Python模块时传入空路径
    mockMvc
        .perform(
            post("/python-save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"testModule\",\"path\":\"\",\"engineType\":\"python\",\"isLoad\":1,\"isExpire\":0}"))
        .andExpect(status().isBadRequest());
  }

  /** 测试检查Python模块文件是否存在功能 */
  @Test
  public void testPythonFileExist() throws Exception {
    // 测试检查Python模块文件是否存在
    mockMvc
        .perform(get("/python-file-exist").param("fileName", "testModule.py"))
        .andExpect(status().isOk());

    // 测试检查Python模块文件是否存在时传入空文件名
    mockMvc
        .perform(get("/python-file-exist").param("fileName", ""))
        .andExpect(status().isBadRequest());

    // 测试检查Python模块文件是否存在时未传入文件名
    mockMvc.perform(get("/python-file-exist")).andExpect(status().isBadRequest());
  }
}
