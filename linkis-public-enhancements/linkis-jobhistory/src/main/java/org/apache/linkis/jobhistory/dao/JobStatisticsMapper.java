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

import org.apache.linkis.jobhistory.entity.JobStatistics;

import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface JobStatisticsMapper {

  JobStatistics taskExecutionStatistics(
      @Param("umUser") String username,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("engineType") String engineType);

  JobStatistics taskExecutionStatisticsWithUserCreator(
      @Param("umUser") String username,
      @Param("userCreatorKey") String userCreatorKey,
      @Param("userCreatorValue") String userCreator,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("engineType") String engineType);

  JobStatistics taskExecutionStatisticsWithCreatorOnly(
      @Param("umUser") String username,
      @Param("userCreatorKey") String userCreatorKey,
      @Param("creator") String userCreator,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("engineType") String engineType);

  JobStatistics engineExecutionStatisticsWithUserCreator(
      @Param("umUser") String username,
      @Param("userCreatorValue") String userCreator,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("engineType") String engineType);

  JobStatistics engineExecutionStatistics(
      @Param("umUser") String username,
      @Param("creator") String userCreator,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("engineType") String engineType);
}
