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

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface JobHistoryMapper {

  List<JobHistory> selectJobHistory(JobHistory jobReq);

  void insertJobHistory(JobHistory jobReq);

  void updateJobHistory(JobHistory jobReq);

  List<JobHistory> searchWithIdOrderAsc(
      @Param("id") Long id,
      @Param("umUser") String username,
      @Param("status") List<String> status,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("engineType") String engineType);

  List<JobHistory> search(
      @Param("id") Long id,
      @Param("umUser") String username,
      @Param("status") List<String> status,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("engineType") String engineType,
      @Param("startId") Long startId,
      @Param("instances") String instances);

  List<JobHistory> searchWithUserCreator(
      @Param("id") Long id,
      @Param("umUser") String username,
      @Param("userCreatorKey") String userCreatorKey,
      @Param("userCreatorValue") String userCreator,
      @Param("status") List<String> status,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("engineType") String engineType,
      @Param("startId") Long startId,
      @Param("instances") String instances);

  List<JobHistory> searchWithCreatorOnly(
      @Param("id") Long id,
      @Param("umUser") String username,
      @Param("userCreatorKey") String userCreatorKey,
      @Param("creator") String userCreator,
      @Param("status") List<String> status,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("engineType") String engineType,
      @Param("startId") Long startId,
      @Param("instances") String instances);

  Integer countUndoneTaskNoCreator(
      @Param("umUser") String username,
      @Param("status") List<String> status,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("engineType") String engineType,
      @Param("startId") Long startId);

  Integer countUndoneTaskWithUserCreator(
      @Param("umUser") String username,
      @Param("userCreatorKey") String userCreatorKey,
      @Param("userCreatorValue") String userCreator,
      @Param("status") List<String> status,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("engineType") String engineType,
      @Param("startId") Long startId);

  Integer countUndoneTaskWithCreatorOnly(
      @Param("umUser") String username,
      @Param("userCreatorKey") String userCreatorKey,
      @Param("creator") String userCreator,
      @Param("status") List<String> status,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("engineType") String engineType,
      @Param("startId") Long startId);

  String selectJobHistoryStatusForUpdate(Long jobId);

  void updateOberverById(@Param("taskid") Long taskid, @Param("observeInfo") String observeInfo);

  void updateJobHistoryCancelById(
      @Param("idList") List<Long> idList, @Param("errorDesc") String errorDesc);
}
