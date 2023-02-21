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
import org.apache.linkis.governance.common.entity.job.SubJobDetail;
import org.apache.linkis.governance.common.entity.job.SubJobInfo;
import org.apache.linkis.governance.common.protocol.job.*;
import org.apache.linkis.jobhistory.dao.JobDetailMapper;
import org.apache.linkis.jobhistory.service.impl.JobHistoryDetailQueryServiceImpl;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;

import java.util.ArrayList;
import java.util.Date;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JobHistoryDetailQueryServiceTest {

  @InjectMocks JobHistoryDetailQueryServiceImpl service;

  @Mock JobDetailMapper mapper;

  /**
   * User-created test data, if it is an auto-increment id, it should not be assigned CURD should be
   * based on the data created by this method insert
   *
   * @return SubJobInfo
   */
  private SubJobInfo buildSubJobInfo() {
    SubJobDetail subJobDetail = new SubJobDetail();
    subJobDetail.setJobGroupId(1L);
    subJobDetail.setResultLocation("test-resultLocation");
    subJobDetail.setResultSize(2);
    subJobDetail.setExecutionContent("test-executionContent");
    subJobDetail.setJobGroupInfo("test-jobGroupInfo");
    subJobDetail.setCreatedTime(new Date());
    subJobDetail.setUpdatedTime(new Date());
    subJobDetail.setStatus("test-status");
    subJobDetail.setPriority(0);

    SubJobInfo subJobInfo = new SubJobInfo();
    subJobInfo.setSubJobDetail(subJobDetail);
    subJobInfo.setStatus("test-status");
    subJobInfo.setCode("test-code");
    JobRequest jobRequest = new JobRequest();
    jobRequest.setId(0L);
    jobRequest.setReqId("");
    jobRequest.setSubmitUser("");
    jobRequest.setExecuteUser("");
    jobRequest.setSource(Maps.newHashMap());
    jobRequest.setExecutionCode("");
    jobRequest.setLabels(Lists.newArrayList());
    jobRequest.setParams(Maps.newHashMap());
    jobRequest.setProgress("");
    jobRequest.setStatus("");
    jobRequest.setLogPath("");
    jobRequest.setErrorCode(0);
    jobRequest.setErrorDesc("");
    jobRequest.setCreatedTime(new Date());
    jobRequest.setUpdatedTime(new Date());
    jobRequest.setInstances("");
    jobRequest.setMetrics(Maps.newHashMap());
    jobRequest.setObserveInfo("");

    subJobInfo.setJobReq(jobRequest);
    subJobInfo.setProgress(0.0F);
    subJobInfo.setProgressInfoMap(Maps.newHashMap());
    return subJobInfo;
  }

  @Test
  void testAdd() {
    JobDetailReqInsert reqInsert = new JobDetailReqInsert(buildSubJobInfo());
    JobRespProtocol jobRespProtocol = service.add(reqInsert);
    assertEquals(jobRespProtocol.getStatus(), 0);
  }

  @Test
  void testChange() {

    JobDetailReqUpdate jobDetailReqUpdate = new JobDetailReqUpdate(buildSubJobInfo());
    JobRespProtocol jobRespProtocol = service.change(jobDetailReqUpdate);
    assertEquals(jobRespProtocol.getStatus(), 0);
  }

  @Test
  void testBatchChange() {
    ArrayList list = new ArrayList<SubJobInfo>();
    list.add(buildSubJobInfo());
    list.add(buildSubJobInfo());
    JobDetailReqBatchUpdate jobDetailReqBatchUpdate = new JobDetailReqBatchUpdate(list);
    ArrayList<JobRespProtocol> jobRespProtocolArrayList =
        service.batchChange(jobDetailReqBatchUpdate);

    // list is matched with the predicate of stream for assertion judgment
    Predicate<JobRespProtocol> statusPrecate = e -> e.getStatus() == 0;
    assertEquals(2, jobRespProtocolArrayList.size());
    assertTrue(jobRespProtocolArrayList.stream().anyMatch(statusPrecate));
  }

  @Test
  void testQuery() {
    SubJobDetail subJobDetail = new SubJobDetail();
    subJobDetail.setId(0L);
    subJobDetail.setJobGroupId(0L);
    subJobDetail.setResultLocation("");
    subJobDetail.setResultSize(0);
    subJobDetail.setExecutionContent("");
    subJobDetail.setJobGroupInfo("");
    subJobDetail.setCreatedTime(new Date());
    subJobDetail.setUpdatedTime(new Date());
    subJobDetail.setStatus("");
    subJobDetail.setPriority(0);

    JobDetailReqQuery jobDetailReqQuery = new JobDetailReqQuery(subJobDetail);
    JobRespProtocol jobRespProtocol = service.query(jobDetailReqQuery);
    assertEquals(jobRespProtocol.getStatus(), 0);
  }

  @Test
  void testUserCreatorLabel() {
    UserCreatorLabel fakeLabel = new UserCreatorLabel();
    fakeLabel.setUser("user");
    fakeLabel.setCreator("creator");
    String userCreator = fakeLabel.getStringValue();
    assertEquals(userCreator, "user-creator");
    assertEquals(fakeLabel.getLabelKey(), "userCreator");
    try {
      assertDoesNotThrow(() -> fakeLabel.valueCheck(fakeLabel.getStringValue()));
      assertThrows(Exception.class, () -> fakeLabel.valueCheck("fake-label-error"));
    } catch (Exception e) {

    }
  }
}
