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

package com.webank.wedatasphere.linkis.engine.execute

import com.webank.wedatasphere.linkis.engine.{PropertiesExecuteRequest, ResourceExecuteRequest}
import com.webank.wedatasphere.linkis.protocol.engine.RequestTask
import com.webank.wedatasphere.linkis.scheduler.executer.{ErrorExecuteResponse, ExecuteRequest, JobExecuteRequest, RunTypeExecuteRequest}
import com.webank.wedatasphere.linkis.scheduler.queue.{Job, JobInfo}
import java.util
/**
  * Created by enjoyyin on 2018/9/25.
  */
abstract class EngineJob extends Job with SenderContainer{
  val resourcesStr:String = "resources"
}

class CommonEngineJob extends EngineJob with SyncSenderContainer {
  protected var request: RequestTask = _
  override def init(): Unit = {}

  def setRequestTask(request: RequestTask) = this.request = request
  def getRequestTask = request


  override def isJobSupportRetry: Boolean = false

  override protected def jobToExecuteRequest: ExecuteRequest = {

    if (request.getProperties.containsKey("runType") && request.getProperties.containsKey(RequestTask.RESULT_SET_STORE_PATH))
      return  new ExecuteRequest with JobExecuteRequest with StorePathExecuteRequest
        with RunTypeExecuteRequest with ResourceExecuteRequest with PropertiesExecuteRequest {
        override val code: String = request.getCode
        override val jobId: String = CommonEngineJob.this.getId
        override val storePath: String = request.getProperties.get(RequestTask.RESULT_SET_STORE_PATH).toString
        override val runType: String = if (request.getProperties.get("runType") != null) {
          request.getProperties.get("runType").toString
        } else "sql"
        override def resources: util.List[Object] = properties.get(resourcesStr) match {
          case rs:util.List[Object] => rs
          case _ => logger.warn(s"${CommonEngineJob.this.getId} resources type is not correct")
            null
        }
        override val properties: util.Map[String, Object] = request.getProperties
      }

    if(request.getProperties.containsKey(RequestTask.RESULT_SET_STORE_PATH)) new ExecuteRequest with JobExecuteRequest with StorePathExecuteRequest {
      override val code: String = request.getCode
      override val jobId: String = CommonEngineJob.this.getId
      override val storePath: String = request.getProperties.get(RequestTask.RESULT_SET_STORE_PATH).toString
    } else new ExecuteRequest with JobExecuteRequest {
      override val code: String = request.getCode
      override val jobId: String = CommonEngineJob.this.getId
    }
  }

  override def getName: String = getId

  override def getJobInfo: JobInfo = new JobInfo(getId, null, getState.toString, getProgress, s"StartTime: $createTime, endTime: $endTime.")  //TODO


  override protected def existsJobDaemon: Boolean = true

  override def close(): Unit = kill()
}