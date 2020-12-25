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

package com.webank.wedatasphere.linkis.entrance.job

/**
  * Created by allenlliu on 2018/12/5.
  */
class SparkEntranceJob extends EntranceExecutionJob{
}
/*
//trait RunTypeExecuteRequest {
//  val runType: String
//}
class SparkEntranceExecuteRequest(job:Job)
  extends ExecuteRequest with RunTypeExecuteRequest with LockExecuteRequest with JobExecuteRequest with StorePathExecuteRequest{

  private val defaultRunType = "sql"

  override val code: String = {
    job match{
      case entranceJob:EntranceJob => entranceJob.getTask.asInstanceOf[RequestPersistTask].getExecutionCode
      case _ => ""
    }
  }
  override val runType: String = {
    job match {
      case entranceJob:EntranceJob => entranceJob.getTask.asInstanceOf[RequestPersistTask].getRunType
      case _ => defaultRunType
    }
  }
  override val lock: String = job.asInstanceOf[EntranceJob].getLock
  override val jobId: String = job.getId
  override val storePath: String = job.asInstanceOf[EntranceJob].getTask match {
    case requestPersistTask:RequestPersistTask => requestPersistTask.getResultLocation
    case _ => ""
  }
}*/
