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

package org.apache.linkis.jobhistory.dao;

import org.apache.linkis.jobhistory.entity.JobDetail;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JobDetailMapperTest extends BaseDaoTest {

  @Autowired JobDetailMapper jobDetailMapper;

  JobDetail insert() {
    JobDetail jobDetail = new JobDetail();
    jobDetail.setJobHistoryId(1L);
    jobDetail.setResultLocation("resultLocation");
    jobDetail.setResultArraySize(1);
    jobDetail.setExecutionContent("executionContent");
    jobDetail.setJobGroupInfo("jobGroupInfo");
    jobDetail.setCreatedTime(new Date());
    jobDetail.setUpdatedTime(new Date());
    jobDetail.setStatus("status");
    jobDetail.setPriority(1);
    jobDetailMapper.insertJobDetail(jobDetail);
    return jobDetail;
  }

  @Test
  void selectJobDetailByJobHistoryId() {
    insert();
    List<JobDetail> jobDetails = jobDetailMapper.selectJobDetailByJobHistoryId(1l);
    Assertions.assertTrue(jobDetails.size() > 0);
  }

  @Test
  void selectJobDetailByJobDetailId() {
    JobDetail insert = insert();
    JobDetail jobDetail = jobDetailMapper.selectJobDetailByJobDetailId(insert.getId());
    Assertions.assertTrue(jobDetail != null);
  }

  @Test
  void insertJobDetail() {
    JobDetail insert = insert();
    Assertions.assertTrue(insert.getId() > 0);
  }

  @Test
  void updateJobDetail() {
    JobDetail insert = insert();
    JobDetail jobDetail = new JobDetail();
    jobDetail.setResultLocation("resultLocation2");
    jobDetail.setResultArraySize(2);
    jobDetail.setExecutionContent("executionContent2");
    jobDetail.setJobGroupInfo("jobGroupInfo2");
    jobDetail.setStatus("status2");
    jobDetail.setPriority(2);
    jobDetail.setId(insert.getId());
    jobDetail.setUpdatedTime(new Date(System.currentTimeMillis() + 60 * 1000));
    jobDetailMapper.updateJobDetail(jobDetail);
    JobDetail queryJobDetail = jobDetailMapper.selectJobDetailByJobDetailId(insert.getId());
    Assertions.assertTrue(queryJobDetail.getStatus().equals("status2"));
    Assertions.assertTrue(queryJobDetail.getPriority().equals(2));
  }

  @Test
  void selectJobDetailStatusForUpdateByJobDetailId() {
    insert();
    JobDetail insert = insert();
    String jobDetailStatus =
        jobDetailMapper.selectJobDetailStatusForUpdateByJobDetailId(insert.getId());
    Assertions.assertTrue(jobDetailStatus.equals("status"));
  }
}
