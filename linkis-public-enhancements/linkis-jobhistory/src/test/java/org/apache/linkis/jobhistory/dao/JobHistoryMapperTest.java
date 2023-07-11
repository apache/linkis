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

import org.apache.linkis.jobhistory.entity.JobHistory;

import org.apache.commons.lang3.time.DateUtils;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobHistoryMapperTest extends BaseDaoTest {

  private static final Logger LOG = LoggerFactory.getLogger(JobHistoryMapperTest.class);

  @Autowired private JobHistoryMapper jobHistoryMapper;

  private JobHistory createJobHistory() {
    JobHistory jobHistory = new JobHistory();
    jobHistory.setJobReqId("LINKISCLI_hadoop_spark_1");
    jobHistory.setSubmitUser("hadoop");
    jobHistory.setExecuteUser("hadoop");
    jobHistory.setSource("{\"scriptPath\":\"LinkisCli\",\"requestIP\":\"127.0.0.1\"}");
    jobHistory.setLabels(
        "{\"userCreator\":\"hadoop-LINKISCLI\",\"engineType\":\"spark-3.0.1\",\"codeType\":\"sql\",\"executeOnce\":\"\"}");
    jobHistory.setParams(
        "{\"configuration\":{\"startup\":{},\"runtime\":{\"hive.resultset.use.unique.column.names\":true,\"wds.linkis.resultSet.store.path\":\"hdfs:///tmp/linkis/hadoop/linkis/20220714_190204/LINKISCLI/3\",\"source\":{\"scriptPath\":\"LinkisCli\",\"requestIP\":\"127.0.0.1\"},\"job\":{\"resultsetIndex\":0,\"#rt_rs_store_path\":\"hdfs:///tmp/linkis/hadoop/linkis/20220714_190204/LINKISCLI/3\"}}},\"variable\":{}}");
    jobHistory.setParams("1.0");
    jobHistory.setStatus("Succeed");
    jobHistory.setLogPath("hdfs:///tmp/linkis/log/2022-07-14/LINKISCLI/hadoop/3.log");
    jobHistory.setErrorCode(0);
    jobHistory.setCreatedTime(new Date());
    jobHistory.setUpdatedTime(new Date());
    jobHistory.setInstances("127.0.0.1:9104");
    jobHistory.setMetrics(
        "{\"scheduleTime\":\"2022-07-14T19:02:05+0800\",\"timeToOrchestrator\":\"2022-07-14T19:02:05+0800\",\"submitTime\":\"2022-07-14T19:02:04+0800\",\"yarnResource\":{\"application_1657595967414_0005\":{\"queueMemory\":1073741824,\"queueCores\":1,\"queueInstances\":0,\"jobStatus\":\"COMPLETED\",\"queue\":\"default\"}},\"completeTime\":\"2022-07-14T19:03:08+0800\"}");
    jobHistory.setEngineType("spark");
    jobHistory.setExecutionCode("show databases;");
    jobHistory.setResultLocation("hdfs:///tmp/linkis/hadoop/linkis/20220714_185840/LINKISCLI/1");
    jobHistory.setObserveInfo("");
    return jobHistory;
  }

  @Test
  @DisplayName("selectJobHistoryTest")
  public void selectJobHistoryTest() {
    JobHistory jobHistory = new JobHistory();
    jobHistory.setId(1L);
    List<JobHistory> histories = jobHistoryMapper.selectJobHistory(jobHistory);
    Assertions.assertTrue(histories.size() > 0);
  }

  @Test
  @DisplayName("insertJobHistoryTest")
  public void insertJobHistoryTest() {

    JobHistory jobHistory = createJobHistory();
    jobHistoryMapper.insertJobHistory(jobHistory);
    List<JobHistory> histories = jobHistoryMapper.selectJobHistory(jobHistory);
    Assertions.assertTrue(histories.size() == 1);
  }

  @Test
  @DisplayName("updateJobHistoryTest")
  public void updateJobHistoryTest() {
    JobHistory jobHistory = createJobHistory();
    jobHistory.setId(1L);
    jobHistoryMapper.updateJobHistory(jobHistory);
    List<JobHistory> histories = jobHistoryMapper.selectJobHistory(jobHistory);
    Assertions.assertEquals("LINKISCLI_hadoop_spark_1", histories.get(0).getJobReqId());
  }

  @Test
  @DisplayName("searchWithIdOrderAscTest")
  public void searchWithIdOrderAscTest() {

    List<String> status = new ArrayList<>();
    status.add("Succeed");
    Date eDate = new Date(System.currentTimeMillis());
    Date sDate = DateUtils.addDays(eDate, -1);
    List<JobHistory> histories = jobHistoryMapper.searchWithIdOrderAsc(sDate, eDate, 1L, status);
    Assertions.assertTrue(histories.size() > 0);
  }

  @Test
  @DisplayName("searchTest")
  public void searchTest() {

    List<String> status = new ArrayList<>();
    status.add("Succeed");
    List<JobHistory> histories =
        jobHistoryMapper.search(1L, "hadoop", status, null, null, "spark", 1L, null);
    Assertions.assertTrue(histories.size() > 0);
  }

  @Test
  @DisplayName("searchWithUserCreatorTest")
  public void searchWithUserCreatorTest() {

    List<String> status = new ArrayList<>();
    status.add("Succeed");
    List<JobHistory> histories =
        jobHistoryMapper.searchWithUserCreator(
            1L, "hadoop", null, null, status, null, null, "spark", 1L, null);
    Assertions.assertTrue(histories.size() > 0);
  }

  @Test
  @DisplayName("searchWithCreatorOnlyTest")
  public void searchWithCreatorOnlyTest() {

    List<String> status = new ArrayList<>();
    status.add("Succeed");
    List<JobHistory> histories =
        jobHistoryMapper.searchWithCreatorOnly(
            1L, "hadoop", null, "hadoop", status, null, null, "spark", 1L, null);
    Assertions.assertTrue(histories.size() > 0);
  }

  @Test
  @DisplayName("countUndoneTaskNoCreatorTest")
  public void countUndoneTaskNoCreatorTest() {
    List<String> status = new ArrayList<>();
    status.add("Succeed");
    Integer counts =
        jobHistoryMapper.countUndoneTaskNoCreator("hadoop", status, null, null, "spark", 1L);
    Assertions.assertTrue(counts.intValue() > 0);
  }

  @Test
  @DisplayName("countUndoneTaskWithUserCreatorTest")
  public void countUndoneTaskWithUserCreatorTest() {
    List<String> status = new ArrayList<>();
    status.add("Succeed");
    Integer counts =
        jobHistoryMapper.countUndoneTaskWithUserCreator(
            "hadoop", null, "hadoop", status, null, null, "spark", 1L);
    Assertions.assertTrue(counts.intValue() > 0);
  }

  @Test
  @DisplayName("selectJobHistoryStatusForUpdateTest")
  public void selectJobHistoryStatusForUpdateTest() {

    String status = jobHistoryMapper.selectJobHistoryStatusForUpdate(1L);
    Assertions.assertEquals("Succeed", status);
  }
}
