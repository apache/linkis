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

package org.apache.linkis.metadata.query.server.restful;

import org.apache.linkis.common.utils.JsonUtils;
import org.apache.linkis.server.Message;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.MultiValueMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MvcUtils {
  private MockMvc mockMvc;

  public MvcUtils(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  public MvcResult buildMvcResultGet(String url) throws Exception {
    MvcResult mvcResult =
        mockMvc
            .perform(get(url))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    return mvcResult;
  }

  public MvcResult buildMvcResultGet(String url, MultiValueMap<String, String> params)
      throws Exception {
    MvcResult mvcResult =
        mockMvc
            .perform(get(url).params(params))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    return mvcResult;
  }

  public MvcResult buildMvcResultPost(String url, String json) throws Exception {
    MvcResult mvcResult =
        mockMvc
            .perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    return mvcResult;
  }

  public MvcResult buildMvcResultPost(String url) throws Exception {
    MvcResult mvcResult =
        mockMvc
            .perform(post(url))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    return mvcResult;
  }

  public MvcResult buildMvcResultPut(String url, String json) throws Exception {
    MvcResult mvcResult =
        mockMvc
            .perform(put(url).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    return mvcResult;
  }

  public MvcResult buildMvcResultPut(String url) throws Exception {
    MvcResult mvcResult =
        mockMvc
            .perform(put(url))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    return mvcResult;
  }

  public MvcResult buildMvcResultDelete(String url) throws Exception {
    MvcResult mvcResult =
        mockMvc
            .perform(delete(url))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    return mvcResult;
  }

  public Message getMessage(MvcResult mvcResult) throws Exception {
    Message res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);
    return res;
  }
}
