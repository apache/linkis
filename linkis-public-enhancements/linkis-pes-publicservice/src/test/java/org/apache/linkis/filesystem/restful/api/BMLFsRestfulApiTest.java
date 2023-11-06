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

package org.apache.linkis.filesystem.restful.api;

import org.apache.linkis.common.utils.JsonUtils;
import org.apache.linkis.filesystem.Scan;
import org.apache.linkis.filesystem.WebApplicationServer;
import org.apache.linkis.filesystem.bml.BMLHelper;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.MessageStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {WebApplicationServer.class, Scan.class})
@AutoConfigureMockMvc
public class BMLFsRestfulApiTest {

  private static final Logger LOG = LoggerFactory.getLogger(BMLFsRestfulApiTest.class);

  @Autowired private MockMvc mockMvc;

  @InjectMocks private BMLFsRestfulApi bmlFsRestfulApi;

  @MockBean private BMLHelper bmlHelper;

  @Test
  @DisplayName("openScriptFromBMLTest")
  public void openScriptFromBMLTest() throws Exception {
    String querySql = this.getClass().getResource("/").getPath() + "/query.sql";
    Map<String, Object> query = new HashMap<>();
    InputStream is = new FileInputStream(new File(querySql));
    query.put("stream", is);
    query.put("name", "hadoop");
    Mockito.when(bmlHelper.query("hadoop", "1", "1")).thenReturn(query);

    MvcResult mvcResult =
        mockMvc
            .perform(
                get("/filesystem/openScriptFromBML")
                    .param("fileName", querySql)
                    .param("resourceId", "1")
                    .param("version", "1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

    Message res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);

    assertEquals(MessageStatus.SUCCESS(), res.getStatus());
    LOG.info(mvcResult.getResponse().getContentAsString());
  }

  @Test
  @DisplayName("openScriptFromProductBMLTest")
  public void openScriptFromProductBMLTest() throws Exception {

    String querySql = this.getClass().getResource("/").getPath() + "/query.sql";
    Map<String, Object> query = new HashMap<>();
    InputStream is = new FileInputStream(new File(querySql));
    query.put("stream", is);
    query.put("name", "hadoop");
    Mockito.when(bmlHelper.query("hadoop", "1", "1")).thenReturn(query);

    MvcResult mvcResult =
        mockMvc
            .perform(
                get("/filesystem/product/openScriptFromBML")
                    .param("fileName", querySql)
                    .param("resourceId", "1")
                    .param("version", "1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

    Message res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);

    assertEquals(MessageStatus.SUCCESS(), res.getStatus());
    LOG.info(mvcResult.getResponse().getContentAsString());
  }
}
