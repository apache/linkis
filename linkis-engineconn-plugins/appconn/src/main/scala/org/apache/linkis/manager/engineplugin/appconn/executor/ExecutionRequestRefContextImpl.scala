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

package org.apache.linkis.manager.engineplugin.appconn.executor

import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.governance.common.entity.job.JobRequestWithDetail
import org.apache.linkis.governance.common.protocol.job.{JobReqQuery, JobRespProtocol}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.rpc.Sender

import java.util

import scala.collection.JavaConversions._

import com.webank.wedatasphere.dss.standard.app.development.listener.conf.RefExecutionConfiguration
import com.webank.wedatasphere.dss.standard.app.development.listener.core.LinkisJob
import com.webank.wedatasphere.dss.standard.app.development.listener.exception.AppConnExecutionErrorException

/**
 * Only jobhistory of linkis1.0 is applicable.
 */
class ExecutionRequestRefContextImpl(
    engineExecutorContext: EngineExecutionContext,
    user: String,
    submitUser: String
) extends AbstractExecutionRequestRefContext(engineExecutorContext, user, submitUser) {

  private val jobHistorySender: Sender =
    Sender.getSender(RefExecutionConfiguration.JOB_HISTORY_APPLICATION_NAME.getValue)

  override def fetchLinkisJob(jobId: Long): LinkisJob = {
    val jobRequest: JobRequestWithDetail = new JobRequestWithDetail()
    jobRequest.setId(jobId)
    jobRequest.setSource(null)
    val requestQueryTask = JobReqQuery(jobRequest)
    val linkisJob: JobRequestWithDetail =
      Utils.tryThrow(jobHistorySender.ask(requestQueryTask) match {
        case responsePersist: JobRespProtocol =>
          val status = responsePersist.getStatus
          if (status != 0) {
            error(s"Fetch linkisJob from jobHistory failed, errorMsg: ${responsePersist.getMsg}.")
            throw new AppConnExecutionErrorException(
              95541,
              s"Fetch linkisJob from jobHistory failed, errorMsg: ${responsePersist.getMsg}."
            )
          } else {
            val data = responsePersist.getData
            data.get("jobHistoryList") match {
              case tasks: util.List[JobRequestWithDetail] =>
                if (tasks.size() > 0) tasks.get(0)
                else
                  throw new AppConnExecutionErrorException(
                    95542,
                    s"query from jobhistory not a correct List type taskId is $jobId"
                  )

              case _ =>
                throw new AppConnExecutionErrorException(
                  95541,
                  s"query from jobhistory not a correct List type taskId is $jobId"
                )
            }
          }

        case r =>
          error(s"Fetch linkisJob from jobHistory incorrectly, response is $r.")
          throw new AppConnExecutionErrorException(
            95541,
            s"Fetch linkisJob from jobHistory incorrectly, response is $r."
          )
      }) {
        case errorException: ErrorException => errorException
        case e: Exception =>
          new AppConnExecutionErrorException(95541, s"query taskId $jobId error.", e)
      }
    new LinkisJob {
      override def getResultLocation: String = linkisJob.getSubJobDetailList
        .find(subJob => { null != subJob.getResultLocation })
        .get
        .getResultLocation
      override def getSubmitUser: String = linkisJob.getSubmitUser
      override def getExecuteUser: String = linkisJob.getSubmitUser
      override def getStatus: String = linkisJob.getStatus
      override def getSource: util.Map[String, String] = linkisJob.getSource
      override def getParams: util.Map[String, AnyRef] = linkisJob.getParams
      override def getLabels: util.List[Label[_]] = linkisJob.getLabels
    }
  }

}
