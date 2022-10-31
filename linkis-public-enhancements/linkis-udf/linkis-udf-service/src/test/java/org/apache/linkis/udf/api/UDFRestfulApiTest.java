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

import org.apache.linkis.common.utils.JsonUtils;
import org.apache.linkis.publicservice.common.lock.service.CommonLockService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.MessageStatus;
import org.apache.linkis.udf.Scan;
import org.apache.linkis.udf.WebApplicationServer;
import org.apache.linkis.udf.dao.UDFDao;
import org.apache.linkis.udf.dao.UDFTreeDao;
import org.apache.linkis.udf.entity.UDFInfo;
import org.apache.linkis.udf.entity.UDFTree;
import org.apache.linkis.udf.service.UDFService;
import org.apache.linkis.udf.service.UDFTreeService;
import org.apache.linkis.udf.vo.UDFAddVo;
import org.apache.linkis.udf.vo.UDFUpdateVo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {WebApplicationServer.class, Scan.class})
@AutoConfigureMockMvc
public class UDFRestfulApiTest {

  private static final Logger LOG = LoggerFactory.getLogger(UDFRestfulApiTest.class);

  @Autowired protected MockMvc mockMvc;

  @MockBean(name = "udfService")
  private UDFService udfService;

  @MockBean(name = "udfTreeService")
  private UDFTreeService udfTreeService;

  @MockBean(name = "CommonLockService")
  private CommonLockService commonLockService;

  @MockBean(name = "udfDao")
  private UDFDao udfDao;

  @MockBean(name = "udfTreeDao")
  private UDFTreeDao udfTreeDao;

  @Test
  @DisplayName("allUDFTest")
  public void allUDFTest() throws Exception {

    MvcResult mvcResult =
        mockMvc
            .perform(post("/udf/all"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

    Message res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);

    assertEquals(MessageStatus.SUCCESS(), res.getStatus());
    LOG.info(mvcResult.getResponse().getContentAsString());

    Map<String, Object> param = new HashMap<>();
    param.put("type", "self");
    param.put("treeId", -1);
    param.put("category", "all");
    String jsonString = new Gson().toJson(param);

    Mockito.when(
            udfTreeService.getTreeById(
                Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .thenReturn(new UDFTree());

    mvcResult =
        mockMvc
            .perform(post("/udf/all").param("jsonString", jsonString))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);

    assertEquals(MessageStatus.SUCCESS(), res.getStatus());
    LOG.info(mvcResult.getResponse().getContentAsString());
  }

  @Test
  @DisplayName("listUDFTest")
  public void listUDFTest() throws Exception {

    Map<String, Object> json = new HashMap<>();
    json.put("type", "self");
    json.put("treeId", -1);
    json.put("category", "all");

    MvcResult mvcResult =
        mockMvc
            .perform(post("/udf/list").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError())
            .andReturn();

    Assertions.assertTrue(mvcResult.getResponse().getStatus() == 400);

    mvcResult =
        mockMvc
            .perform(
                post("/udf/list")
                    .content(new Gson().toJson(json))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    Message res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);

    assertEquals(MessageStatus.SUCCESS(), res.getStatus());
    LOG.info(mvcResult.getResponse().getContentAsString());
  }

  @Test
  @DisplayName("addUDFTest")
  public void addUDFTest() throws Exception {

    UDFAddVo udfAddVo = new UDFAddVo();
    udfAddVo.setCreateUser("hadoop");
    udfAddVo.setUdfName("test");
    udfAddVo.setUdfType(3);
    udfAddVo.setTreeId(13L);
    udfAddVo.setSys("IDE");
    udfAddVo.setClusterName("all");
    Map<String, UDFAddVo> paramMap = new HashMap<>();
    paramMap.put("udfAddVo", udfAddVo);

    MvcResult mvcResult =
        mockMvc
            .perform(post("/udf/add").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError())
            .andReturn();
    Assertions.assertTrue(mvcResult.getResponse().getStatus() == 400);

    mvcResult =
        mockMvc
            .perform(
                post("/udf/add")
                    .content(new Gson().toJson(paramMap))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    Message res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);

    assertEquals(MessageStatus.SUCCESS(), res.getStatus());
    LOG.info(mvcResult.getResponse().getContentAsString());
  }

  @Test
  @DisplayName("updateUDFTest")
  public void updateUDFTest() throws Exception {

    UDFUpdateVo udfUpdateVo = new UDFUpdateVo();
    udfUpdateVo.setId(3L);
    udfUpdateVo.setUdfName("test");
    udfUpdateVo.setUdfType(3);
    udfUpdateVo.setPath("file:///home/hadoop/logs/linkis/hadoop/baoyang/udf/scalaUdf.scala");

    MvcResult mvcResult =
        mockMvc
            .perform(post("/udf/update").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError())
            .andReturn();
    Assertions.assertTrue(mvcResult.getResponse().getStatus() == 400);

    mvcResult =
        mockMvc
            .perform(
                post("/udf/update")
                    .content(new Gson().toJson(udfUpdateVo))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    Message res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);
    assertEquals(MessageStatus.SUCCESS(), res.getStatus());
    LOG.info(mvcResult.getResponse().getContentAsString());
  }

  @Test
  @DisplayName("deleteUDFTest")
  public void deleteUDFTest() throws Exception {

    MvcResult mvcResult =
        mockMvc
            .perform(post("/udf/delete").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError())
            .andReturn();
    Assertions.assertTrue(mvcResult.getResponse().getStatus() == 404);

    UDFInfo udfInfo = new UDFInfo();
    udfInfo.setCreateUser("hadoop");
    Mockito.when(udfService.getUDFById(Mockito.anyLong(), Mockito.anyString())).thenReturn(udfInfo);

    Long id = 3L;
    mvcResult =
        mockMvc
            .perform(post("/udf/delete/{id}", id).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    Message res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);
    assertEquals(MessageStatus.SUCCESS(), res.getStatus());
    LOG.info(mvcResult.getResponse().getContentAsString());
  }

  @Test
  @DisplayName("publishUDFTest")
  public void publishUDFTest() throws Exception {

    MvcResult mvcResult =
        mockMvc
            .perform(post("/udf/publish").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError())
            .andReturn();
    Assertions.assertTrue(mvcResult.getResponse().getStatus() == 400);

    Map<String, Object> param = new HashMap<>();
    param.put("udfId", 3L);
    param.put("version", "v000001");

    UDFInfo udfInfo = new UDFInfo();
    udfInfo.setCreateUser("hadoop");
    Mockito.when(udfService.getUDFById(Mockito.anyLong(), Mockito.anyString())).thenReturn(udfInfo);
    Mockito.when(udfService.isUDFManager("hadoop")).thenReturn(true);

    mvcResult =
        mockMvc
            .perform(
                post("/udf/publish")
                    .content(new Gson().toJson(param))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    Message res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);
    assertEquals(MessageStatus.SUCCESS(), res.getStatus());
    LOG.info(mvcResult.getResponse().getContentAsString());
  }

  @Test
  @DisplayName("rollbackUDFTest")
  public void rollbackUDFTest() throws Exception {

    MvcResult mvcResult =
        mockMvc
            .perform(post("/udf/rollback").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError())
            .andReturn();
    Assertions.assertTrue(mvcResult.getResponse().getStatus() == 400);

    Map<String, Object> param = new HashMap<>();
    param.put("udfId", 3L);
    param.put("version", "v000001");

    UDFInfo udfInfo = new UDFInfo();
    udfInfo.setCreateUser("hadoop");
    Mockito.when(udfService.getUDFById(Mockito.anyLong(), Mockito.anyString())).thenReturn(udfInfo);

    mvcResult =
        mockMvc
            .perform(
                post("/udf/rollback")
                    .content(new Gson().toJson(param))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    Message res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);
    assertEquals(MessageStatus.SUCCESS(), res.getStatus());
    LOG.info(mvcResult.getResponse().getContentAsString());
  }

  @Test
  @DisplayName("versionListTest")
  public void versionListTest() throws Exception {

    MvcResult mvcResult =
        mockMvc
            .perform(get("/udf/versionList").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError())
            .andReturn();
    Assertions.assertTrue(mvcResult.getResponse().getStatus() == 400);

    mvcResult =
        mockMvc
            .perform(
                get("/udf/versionList").contentType(MediaType.APPLICATION_JSON).param("udfId", "3"))
            .andExpect(status().isOk())
            .andReturn();
    Message res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);
    assertEquals(MessageStatus.SUCCESS(), res.getStatus());
    LOG.info(mvcResult.getResponse().getContentAsString());
  }

  @Test
  @DisplayName("managerPagesTest")
  public void managerPagesTest() throws Exception {

    MvcResult mvcResult =
        mockMvc
            .perform(post("/udf/managerPages").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError())
            .andReturn();
    Assertions.assertTrue(mvcResult.getResponse().getStatus() == 400);

    HashMap<String, Object> paramMap = new HashMap();
    paramMap.put("udfName", "test");
    paramMap.put("udfType", "3");
    paramMap.put("createUser", "hadoop");
    paramMap.put("curPage", 0);
    paramMap.put("pageSize", 10);

    PageInfo<UDFAddVo> pageInfo = new PageInfo<>();
    pageInfo.setList(new ArrayList<>());
    pageInfo.setPages(10);
    pageInfo.setTotal(100);
    Mockito.when(
            udfService.getManagerPages(
                Mockito.anyString(),
                Mockito.anyCollection(),
                Mockito.anyString(),
                Mockito.anyInt(),
                Mockito.anyInt()))
        .thenReturn(pageInfo);
    mvcResult =
        mockMvc
            .perform(
                post("/udf/managerPages")
                    .content(new Gson().toJson(paramMap))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    Message res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);
    assertEquals(MessageStatus.SUCCESS(), res.getStatus());
    LOG.info(mvcResult.getResponse().getContentAsString());
  }

  @Test
  @DisplayName("allUdfUsersTest")
  public void allUdfUsersTest() throws Exception {

    MvcResult mvcResult =
        mockMvc
            .perform(get("/udf/allUdfUsers").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError())
            .andReturn();
    Assertions.assertTrue(mvcResult.getResponse().getStatus() == 400);

    HashMap<String, Object> paramMap = new HashMap();
    paramMap.put("userName", "hadoop");

    mvcResult =
        mockMvc
            .perform(
                get("/udf/allUdfUsers")
                    .content(new Gson().toJson(paramMap))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    Message res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);
    assertEquals(MessageStatus.SUCCESS(), res.getStatus());
    LOG.info(mvcResult.getResponse().getContentAsString());
  }

  @Test
  @DisplayName("getUserDirectoryTest")
  public void getUserDirectoryTest() throws Exception {

    MvcResult mvcResult =
        mockMvc
            .perform(get("/udf/userDirectory").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError())
            .andReturn();
    Assertions.assertTrue(mvcResult.getResponse().getStatus() == 400);

    String category = "function";
    mvcResult =
        mockMvc
            .perform(
                get("/udf/userDirectory")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("category", category))
            .andExpect(status().isOk())
            .andReturn();
    Message res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);
    assertEquals(MessageStatus.SUCCESS(), res.getStatus());
    LOG.info(mvcResult.getResponse().getContentAsString());
  }
}
