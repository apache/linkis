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
import org.apache.linkis.governance.common.protocol.job._
import org.apache.linkis.jobhistory.conversions.TaskConversions._
import org.apache.linkis.jobhistory.dao.{JobAiHistoryMapper, JobDetailMapper, JobHistoryMapper}
import org.apache.linkis.jobhistory.service.{JobAiHistoryService, JobHistoryDetailQueryService}
import org.apache.linkis.rpc.message.annotation.Receiver

import org.apache.commons.lang3.exception.ExceptionUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class JobAiHistoryServiceImpl extends JobAiHistoryService with Logging {

  @Autowired
  private var jobAiHistoryMapper: JobAiHistoryMapper = _

  @Receiver
  override def insert(jobAiReqInsert: JobAiReqInsert): JobRespProtocol = {
    logger.info("Insert data into the database(往数据库中插入数据)：" + jobAiReqInsert.toString)
    val jobResp = new JobRespProtocol
    Utils.tryCatch {
      val jobInsert = JobAiReqToJobAiHistory(jobAiReqInsert.jobReq)
      jobInsert.setUpdatedTime(jobInsert.getCreatedTime)
      jobAiHistoryMapper.insert(jobInsert)
      jobResp.setStatus(0)
    } { case exception: Exception =>
      logger.error(
        s"Failed to add JobAiReqInsert ${jobAiReqInsert.toString},should be retry",
        exception
      )
      jobResp.setStatus(2)
      jobResp.setMsg(ExceptionUtils.getRootCauseMessage(exception))
    }
    jobResp
  }

}
