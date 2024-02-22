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

package org.apache.linkis.jobhistory.service.impl

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.jobhistory.dao.JobStatisticsMapper
import org.apache.linkis.jobhistory.entity.JobStatistics
import org.apache.linkis.jobhistory.service.JobStatisticsQueryService
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util.Date

@Service
class JobStatisticsQueryServiceImpl extends JobStatisticsQueryService with Logging {

  @Autowired
  private var jobStatisticsMapper: JobStatisticsMapper = _

  override def taskExecutionStatistics(
      sDate: Date,
      eDate: Date,
      username: String,
      creator: String,
      engineType: String
  ): JobStatistics = {
    val result = if (StringUtils.isBlank(creator)) {
      jobStatisticsMapper.taskExecutionStatistics(username, sDate, eDate, engineType)
    } else if (StringUtils.isBlank(username)) {
      val fakeLabel = new UserCreatorLabel
      jobStatisticsMapper.taskExecutionStatisticsWithCreatorOnly(
        username,
        fakeLabel.getLabelKey,
        creator,
        sDate,
        eDate,
        engineType
      )
    } else {
      val fakeLabel = new UserCreatorLabel
      fakeLabel.setUser(username)
      fakeLabel.setCreator(creator)
      val userCreator = fakeLabel.getStringValue
      Utils.tryCatch(fakeLabel.valueCheck(userCreator)) { t =>
        logger.info("input user or creator is not correct", t)
        throw t
      }
      jobStatisticsMapper.taskExecutionStatisticsWithUserCreator(
        username,
        fakeLabel.getLabelKey,
        userCreator,
        sDate,
        eDate,
        engineType
      )
    }
    result
  }

  override def engineExecutionStatistics(
      sDate: Date,
      eDate: Date,
      username: String,
      creator: String,
      engineType: String
  ): JobStatistics = {
    val result = if (StringUtils.isBlank(username) || StringUtils.isBlank(creator)) {
      jobStatisticsMapper.engineExecutionStatistics(username, creator, sDate, eDate, engineType)
    } else {
      val fakeLabel = new UserCreatorLabel
      fakeLabel.setUser(username)
      fakeLabel.setCreator(creator)
      val userCreator = fakeLabel.getStringValue
      Utils.tryCatch(fakeLabel.valueCheck(userCreator)) { t =>
        logger.info("input user or creator is not correct", t)
        throw t
      }
      jobStatisticsMapper.engineExecutionStatisticsWithUserCreator(
        username,
        userCreator,
        sDate,
        eDate,
        engineType
      )
    }
    result
  }

}
