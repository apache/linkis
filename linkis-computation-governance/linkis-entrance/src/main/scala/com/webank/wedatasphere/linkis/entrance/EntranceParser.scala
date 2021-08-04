/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.entrance

import com.webank.wedatasphere.linkis.common.exception.ErrorException
import com.webank.wedatasphere.linkis.entrance.execute.EntranceJob
import com.webank.wedatasphere.linkis.governance.common.entity.job.JobRequest
import com.webank.wedatasphere.linkis.scheduler.queue.Job


abstract class EntranceParser {

  def getEntranceContext: EntranceContext
  def setEntranceContext(entranceContext: EntranceContext): Unit

  @throws[ErrorException]
  def parseToTask(params: java.util.Map[String, Any]): JobRequest

  @throws[ErrorException]
  def parseToJob(jobReq: JobRequest): Job

  @throws[ErrorException]
  def parseToJob(job: Job): JobRequest = job match {
    case entranceJob: EntranceJob => {
      entranceJob.getJobRequest
    }
    case _ =>
      null
  }

}