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

package org.apache.linkis.monitor.jobhistory.dao;

import org.apache.linkis.monitor.jobhistory.entity.JobHistory;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface JobHistoryMapper {

  List<JobHistory> selectJobHistory(JobHistory jobReq);

  List<JobHistory> search(
      @Param("id") Long id,
      @Param("umUser") String username,
      @Param("status") List<String> status,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("engineType") String engineType);

  void updateIncompleteJobStatusGivenIDList(
      @Param("idList") List<Long> idList, @Param("targetStatus") String targetStatus);

  void updateJobStatusForInstanceGivenStatusList(
      @Param("instanceName") String instanceName,
      @Param("statusList") List<String> statusList,
      @Param("targetStatus") String targetStatus,
      @Param("startDate") Date startDate);

  List<JobHistory> searchByCache(
      @Param("id") Long id,
      @Param("umUser") String username,
      @Param("status") List<String> status,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("engineType") String engineType);

  List<JobHistory> searchByCacheAndUpdateTime(
      @Param("id") Long id,
      @Param("umUser") String username,
      @Param("status") List<String> status,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("engineType") String engineType);

  Long selectIdByHalfDay(@Param("id") long beginId);

  Long selectMaxId();
}
