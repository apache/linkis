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

import org.h2.tools.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JobDetailMapperTest extends BaseDaoTest {

  private static final Logger logger = LoggerFactory.getLogger(JobDetailMapperTest.class);

  @Autowired JobDetailMapper jobDetailMapper;

  /**
   * User-created test data, if it is an auto-increment id, it should not be assigned CURD should be
   * based on the data created by this method insert
   *
   * @return JobDetail
   */
  private JobDetail insertOne() {
    // insertOne
    JobDetail jobDetail = new JobDetail();
    jobDetail.setJobHistoryId(0L);
    jobDetail.setResultLocation("/test/location");
    jobDetail.setResultArraySize(2);
    jobDetail.setExecutionContent("excution content");
    jobDetail.setJobGroupInfo("test");
    jobDetail.setCreatedTime(new Date());
    jobDetail.setUpdatedTime(new Date());
    jobDetail.setStatus("success");
    jobDetail.setPriority(0);

    jobDetailMapper.insertJobDetail(jobDetail);
    return jobDetail;
  }

  @BeforeAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void beforeAll() throws Exception {
    // Start the console of h2 to facilitate viewing of h2 data
    Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
  }

  @AfterAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void afterAll() throws Exception {}

  @Test
  void testSelectJobDetailByJobHistoryId() {
    JobDetail jobDetail = insertOne();
    List<JobDetail> result =
        jobDetailMapper.selectJobDetailByJobHistoryId(jobDetail.getJobHistoryId());
    assertNotEquals(result.size(), 0);
  }

  @Test
  void testSelectJobDetailByJobDetailId() {
    JobDetail jobDetail = insertOne();
    JobDetail result = jobDetailMapper.selectJobDetailByJobDetailId(jobDetail.getId());
    assertNotNull(result);
  }

  @Test
  void testInsertJobDetail() {
    JobDetail jobDetail = insertOne();
    assertTrue(jobDetail.getId() > 0);
  }

  @Test
  void testUpdateJobDetail() {

    JobDetail expectedJobDetail = insertOne();
    expectedJobDetail.setResultLocation("modify " + expectedJobDetail.getResultLocation());
    expectedJobDetail.setResultArraySize(10);
    expectedJobDetail.setExecutionContent("modify " + expectedJobDetail.getExecutionContent());
    expectedJobDetail.setJobGroupInfo("modify " + expectedJobDetail.getJobGroupInfo());
    expectedJobDetail.setCreatedTime(new Date());
    expectedJobDetail.setUpdatedTime(new Date());
    expectedJobDetail.setStatus("modify " + expectedJobDetail.getStatus());
    expectedJobDetail.setPriority(1);

    jobDetailMapper.updateJobDetail(expectedJobDetail);

    JobDetail actualJobDetail =
        jobDetailMapper.selectJobDetailByJobDetailId(expectedJobDetail.getId());

    //        assertEquals(expectedJobDetail, actualJobDetail);
    ////       assertThat(actual, samePropertyValuesAs(expected));
    // Determine whether the property values of the two objects are exactly the same
    assertThat(actualJobDetail).usingRecursiveComparison().isEqualTo(expectedJobDetail);
  }

  @Test
  void testSelectJobDetailStatusForUpdateByJobDetailId() {
    JobDetail jobDetail = insertOne();
    String result = jobDetailMapper.selectJobDetailStatusForUpdateByJobDetailId(jobDetail.getId());
    assertNotNull(result);
  }
}
