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
import java.util.Map;

public interface JobHistoryMapper {

  List<JobHistory> selectJobHistory(JobHistory jobReq);

  void insertJobHistory(JobHistory jobReq);

  void updateJobHistory(JobHistory jobReq);

  List<JobHistory> searchWithIdOrderAsc(
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("startId") Long startId,
      @Param("status") List<String> status);

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

  /**
   * query wait for failover job
   *
   * <p>Sql example: SELECT a.* FROM linkis_ps_job_history_group_history a where (a.instances = ''
   * or a.instances is null or a.instances not in ('192.168.1.123:9104','192.168.1.124:9104') or
   * EXISTS ( select 1 from ( select '192.168.1.123:9104' as instances, 1697775054098 as
   * registryTime union all select '192.168.1.124:9104' as instances, 1666239054098 as registryTime
   * ) b where a.instances = b.instances and a.created_time < FROM_UNIXTIME(b.registryTime/1000) ) )
   * and status in ('Inited','Running','Scheduled','WaitForRetry') and a.created_time >=
   * FROM_UNIXTIME(1666239054098/1000) limit 10
   *
   * @param instancesMap
   * @param statusList
   * @param startTimestamp
   * @param limit
   * @return
   */
  List<JobHistory> selectFailoverJobHistory(
      @Param("instancesMap") Map<String, Long> instancesMap,
      @Param("statusList") List<String> statusList,
      @Param("startTimestamp") Long startTimestamp,
      @Param("limit") Integer limit);

  List<JobHistory> taskDurationTopN(
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("umUser") String username,
      @Param("engineType") String engineType);

  List<JobHistory> taskDurationTopNWithUserCreator(
      @Param("umUser") String username,
      @Param("userCreatorKey") String userCreatorKey,
      @Param("userCreatorValue") String userCreator,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("engineType") String engineType);

  List<JobHistory> taskDurationTopNWithCreatorOnly(
      @Param("umUser") String username,
      @Param("userCreatorKey") String userCreatorKey,
      @Param("creator") String userCreator,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("engineType") String engineType);
}
