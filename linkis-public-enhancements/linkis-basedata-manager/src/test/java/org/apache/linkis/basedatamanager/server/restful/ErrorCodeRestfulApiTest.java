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

package org.apache.linkis.basedatamanager.server.restful;

import org.apache.linkis.basedatamanager.server.Scan;
import org.apache.linkis.basedatamanager.server.WebApplicationServer;
import org.apache.linkis.basedatamanager.server.domain.ErrorCodeEntity;
import org.apache.linkis.common.utils.JsonUtils;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.MessageStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith({SpringExtension.class})
@AutoConfigureMockMvc
@SpringBootTest(classes = {WebApplicationServer.class, Scan.class})
public class ErrorCodeRestfulApiTest {
  private Logger logger = LoggerFactory.getLogger(DatasourceTypeRestfulApiTest.class);

  @Autowired protected MockMvc mockMvc;

  @Test
  public void TestList() throws Exception {
    LinkedMultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
    paramsMap.add("searchName", "");
    paramsMap.add("currentPage", "1");
    paramsMap.add("pageSize", "10");
    MvcResult mvcResult =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/basedata-manager/error-code").params(paramsMap))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    Message message =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);
    Assertions.assertEquals(MessageStatus.SUCCESS(), message.getStatus());
    logger.info(String.valueOf(message));
  }

  @Test
  public void TestGet() throws Exception {
    MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
    String url = "/basedata-manager/error-code/" + "1";
    MvcResult mvcResult =
        mockMvc
            .perform(MockMvcRequestBuilders.get(url).params(paramsMap))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    Message message =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);
    Assertions.assertEquals(MessageStatus.SUCCESS(), message.getStatus());
    logger.info(String.valueOf(message));
  }

  @Test
  public void TestAdd() throws Exception {
    String url = "/basedata-manager/error-code";
    ObjectMapper objectMapper = new ObjectMapper();
    ErrorCodeEntity errorCodeEntity = new ErrorCodeEntity();
    errorCodeEntity.setId(10L);
    errorCodeEntity.setErrorCode("test");
    errorCodeEntity.setErrorRegex("test");
    errorCodeEntity.setErrorType(1);
    errorCodeEntity.setErrorDesc("test");
    String msg = objectMapper.writeValueAsString(errorCodeEntity);
    MvcResult mvcResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(msg))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    Message message =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);
    Assertions.assertEquals(MessageStatus.SUCCESS(), message.getStatus());
    logger.info(String.valueOf(message));
  }

  @Test
  public void TestUpdate() throws Exception {
    String url = "/basedata-manager/error-code";
    ObjectMapper objectMapper = new ObjectMapper();
    ErrorCodeEntity errorCodeEntity = new ErrorCodeEntity();
    errorCodeEntity.setId(10L);
    errorCodeEntity.setErrorCode("test");
    errorCodeEntity.setErrorRegex("test");
    errorCodeEntity.setErrorType(1);
    errorCodeEntity.setErrorDesc("test");
    String msg = objectMapper.writeValueAsString(errorCodeEntity);
    MvcResult mvcResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders.put(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(msg))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    Message message =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);
    Assertions.assertEquals(MessageStatus.SUCCESS(), message.getStatus());
    logger.info(String.valueOf(message));
  }

  @Test
  public void TestRemove() throws Exception {
    String url = "/basedata-manager/error-code/" + "1";
    MvcResult mvcResult =
        mockMvc
            .perform(MockMvcRequestBuilders.delete(url))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    Message message =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);
    Assertions.assertEquals(MessageStatus.SUCCESS(), message.getStatus());
    logger.info(String.valueOf(message));
  }
}
