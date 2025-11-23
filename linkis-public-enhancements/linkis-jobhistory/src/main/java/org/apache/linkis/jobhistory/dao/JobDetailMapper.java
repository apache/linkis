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

import java.util.List;

public interface JobDetailMapper {

  List<JobDetail> selectJobDetailByJobHistoryId(Long jobHistoryId);

  JobDetail selectJobDetailByJobDetailId(Long jobId);

  List<JobDetail> queryJobHistoryDetail(JobDetail JobDetail);

  int insertJobDetail(JobDetail insertJob);

  void updateJobDetail(JobDetail updateJob);

  //  def search(@Param("jobId") taskID: Long, @Param("umUser") username: String, @Param("status")
  // status: util.List[String],
  //             @Param("startDate") startDate: Date, @Param("endDate") endDate: Date,
  // @Param("executeApplicationName") executeApplicationName: String,
  //             @Param("instance") instance: String, @Param("execId") execId: String))

  String selectJobDetailStatusForUpdateByJobDetailId(Long jobId);

  //  List<String> selectJobStatusForUpdateByJobHistoryId(Long jobId);
}
