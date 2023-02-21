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

package org.apache.linkis.jobhistory.service;

import org.apache.linkis.governance.common.entity.job.JobRequest;
import org.apache.linkis.governance.common.protocol.job.*;
import org.apache.linkis.jobhistory.dao.JobHistoryMapper;
import org.apache.linkis.jobhistory.entity.JobHistory;
import org.apache.linkis.jobhistory.service.impl.JobHistoryQueryServiceImpl;

import java.util.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JobHistoryQueryServiceTest {

  @InjectMocks JobHistoryQueryServiceImpl jobRequestQueryService;

  @Mock JobHistoryMapper jobRequestMapper;

  private JobRequest createJobRequest() {
    JobRequest jobRequest = new JobRequest();
    jobRequest.setReqId("LINKISCLI_hadoop_spark_1");
    jobRequest.setSubmitUser("hadoop");
    jobRequest.setExecuteUser("hadoop");
    jobRequest.setSource(new HashMap<>());
    jobRequest.setLabels(new ArrayList<>());
    jobRequest.setParams(new HashMap<>());
    jobRequest.setStatus("Succeed");
    jobRequest.setLogPath("hdfs:///tmp/linkis/log/2022-07-14/LINKISCLI/hadoop/3.log");
    jobRequest.setErrorCode(0);
    jobRequest.setCreatedTime(new Date());
    jobRequest.setUpdatedTime(new Date());
    jobRequest.setInstances("127.0.0.1:9104");
    jobRequest.setMetrics(new HashMap<>());
    jobRequest.setExecutionCode("show databases;");
    jobRequest.setResultLocation("hdfs:///tmp/linkis/hadoop/linkis/20220714_185840/LINKISCLI/1");
    jobRequest.setObserveInfo("");
    return jobRequest;
  }

  @Test
  @DisplayName("addTest")
  public void addTest() {
    JobReqInsert jobReqInsert = new JobReqInsert(createJobRequest());
    JobRespProtocol jobRespProtocol = jobRequestQueryService.add(jobReqInsert);
    Assertions.assertNotNull(jobRespProtocol);
  }

  @Test
  @DisplayName("changeTest")
  public void changeTest() {
    JobReqUpdate jobReqUpdate = new JobReqUpdate(createJobRequest());
    JobRespProtocol jobRespProtocol = jobRequestQueryService.change(jobReqUpdate);
    Assertions.assertNotNull(jobRespProtocol);
  }

  @Test
  @DisplayName("batchChangeTest")
  public void batchChangeTest() {

    JobReqBatchUpdate jobReqBatchUpdate =
        new JobReqBatchUpdate(new ArrayList<>(Arrays.asList(createJobRequest())));
    ArrayList<JobRespProtocol> protocols = jobRequestQueryService.batchChange(jobReqBatchUpdate);
    Assertions.assertTrue(protocols.size() > 0);
  }

  @Test
  @DisplayName("queryTest")
  public void queryTest() {
    JobReqQuery jobReqQuery = new JobReqQuery(createJobRequest());
    JobRespProtocol jobRespProtocol = jobRequestQueryService.query(jobReqQuery);
    Assertions.assertNotNull(jobRespProtocol);
  }

  @Test
  @DisplayName("getJobHistoryByIdAndNameTest")
  public void getJobHistoryByIdAndNameTest() {
    JobHistory history = jobRequestQueryService.getJobHistoryByIdAndName(1L, "hadoop");
    Assertions.assertNull(history);
  }

  @Test
  @DisplayName("searchTest")
  public void searchTest() {
    List<JobHistory> histories =
        jobRequestQueryService.search(
            1L, "hadoop", "hadoop", "Succeed", new Date(), new Date(), "spark", 1L, null);
    Assertions.assertTrue(histories.size() == 0);
  }

  @Test
  @DisplayName("countUndoneTasksTest")
  public void countUndoneTasksTest() {

    Integer counts =
        jobRequestQueryService.countUndoneTasks(
            "hadoop", "hadoop", new Date(), new Date(), "spark", 1L);
    Assertions.assertTrue(counts.intValue() == 0);
  }

  @Test
  @DisplayName("searchOneTest")
  public void searchOneTest() {

    JobHistory jobHistory = jobRequestQueryService.searchOne(1L, new Date(), new Date());
    Assertions.assertNotNull(jobHistory);
  }
}
