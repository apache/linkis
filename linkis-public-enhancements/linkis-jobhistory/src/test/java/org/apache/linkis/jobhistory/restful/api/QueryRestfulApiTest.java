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

package org.apache.linkis.jobhistory.restful.api;

import org.apache.linkis.common.utils.JsonUtils;
import org.apache.linkis.jobhistory.Scan;
import org.apache.linkis.jobhistory.WebApplicationServer;
import org.apache.linkis.jobhistory.conversions.TaskConversions;
import org.apache.linkis.jobhistory.dao.JobDetailMapper;
import org.apache.linkis.jobhistory.entity.JobDetail;
import org.apache.linkis.jobhistory.entity.JobHistory;
import org.apache.linkis.jobhistory.entity.QueryTaskVO;
import org.apache.linkis.jobhistory.service.JobHistoryQueryService;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Date;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** QueryRestfulApi Tester */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {WebApplicationServer.class, Scan.class})
@AutoConfigureMockMvc
public class QueryRestfulApiTest {

  private static final Logger logger = LoggerFactory.getLogger(QueryRestfulApiTest.class);

  @Autowired protected MockMvc mockMvc;

  @MockBean(name = "jobHistoryQueryService")
  private JobHistoryQueryService jobHistoryQueryService;

  @MockBean(name = "jobDetailMapper")
  private JobDetailMapper jobDetailMapper;

  @BeforeAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void beforeAll() throws Exception {
    //        System.getProperties().setProperty(LinkisMainHelper.SERVER_NAME_KEY(),
    // "linkis-ps-publicservice");
    //        LinkisBaseServerApp.main(new String[]{});
    // new SpringApplicationBuilder(QueryRestfulApiTest.class).run("--server.port=2222");
    // logger.info("start linkis-ps-publicservice servive");
  }

  @AfterAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void afterAll() {}

  @Test
  @DisplayName("")
  public void testGovernanceStationAdmin() throws Exception {

    MvcResult mvcResult =
        mockMvc
            .perform(get("/jobhistory/governanceStationAdmin"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

    Message res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);
    assertEquals(MessageStatus.SUCCESS(), res.getStatus());

    logger.info(mvcResult.getResponse().getContentAsString());
  }

  @Test
  public void testGetTaskByID() throws Exception {

    long jobId = 123;
    MvcResult mvcResult =
        mockMvc
            .perform(get("/jobhistory/{id}/get", jobId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

    QueryTaskVO queryTaskVO = new QueryTaskVO();
    queryTaskVO.setSubJobs(Lists.newArrayList());
    queryTaskVO.setEngineStartTime(new Date());
    queryTaskVO.setSourceTailor("test");
    queryTaskVO.setSourceJson("test");
    queryTaskVO.setTaskID(0L);

    // any matcher scene with uncertain parameters todo:mock does not take effect
    MockedStatic<TaskConversions> taskConversionsMockedStatic =
        Mockito.mockStatic(TaskConversions.class);
    when(TaskConversions.jobHistory2TaskVO(any(JobHistory.class), any())).thenReturn(queryTaskVO);
    when(jobDetailMapper.insertJobDetail(new JobDetail())).thenReturn(1);
    Message res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);
    assertEquals(MessageStatus.ERROR(), res.getStatus());
    logger.info(mvcResult.getResponse().getContentAsString());
  }

  @Test
  public void testList() throws Exception {
    // with optional parameters
    MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
    paramsMap.add("startDate", String.valueOf(System.currentTimeMillis()));
    paramsMap.add("endDate", String.valueOf(System.currentTimeMillis()));
    paramsMap.add("status", "1");
    paramsMap.add("pageNow", "1");
    paramsMap.add("pageSize", "15");
    paramsMap.add("taskID", "123");
    paramsMap.add("executeApplicationName", "test_name");
    paramsMap.add("proxyUser", null);

    MvcResult mvcResult =
        mockMvc
            .perform(get("/jobhistory/list").params(paramsMap))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

    Message res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);
    assertEquals(MessageStatus.SUCCESS(), res.getStatus());
    logger.info(mvcResult.getResponse().getContentAsString());

    // without optional parameters
    mvcResult =
        mockMvc
            .perform(get("/jobhistory/list"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

    res =
        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(), Message.class);
    assertEquals(MessageStatus.SUCCESS(), res.getStatus());

    logger.info(mvcResult.getResponse().getContentAsString());
  }
}
