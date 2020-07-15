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
package com.webank.wedatasphere.linkis.entrance.execute

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.entrance.job.EntranceExecutionJob
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask
import com.webank.wedatasphere.linkis.scheduler.executer.{CompletedExecuteResponse, ErrorExecuteResponse, ExecuteRequest}
import com.webank.wedatasphere.linkis.scheduler.queue.Job
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState.Running

class JDBCEntranceJob extends EntranceExecutionJob{

  override def jobToExecuteRequest(): ExecuteRequest = {
    new ExecuteRequest with StorePathExecuteRequest with JDBCJobExecuteRequest {
      override val code: String = JDBCEntranceJob.this.getTask match{
        case requestPersistTask:RequestPersistTask => requestPersistTask.getExecutionCode
        case _ => null
      }
      override val storePath: String = JDBCEntranceJob.this.getTask match{
        case requestPersistTask:RequestPersistTask => requestPersistTask.getResultLocation
        case _ => ""
      }
      override val job: Job = JDBCEntranceJob.this
    }
  }

  //use executor execute jdbc code (使用executor执行jdbc脚本代码)
  override def run(): Unit = {
    if(!isScheduled) return
    startTime = System.currentTimeMillis
    Utils.tryAndWarn(transition(Running))

    val executeResponse = Utils.tryCatch(getExecutor.execute(jobToExecuteRequest())){
      case t: InterruptedException =>
        warn(s"job $toString is interrupted by user!", t)
        ErrorExecuteResponse("job is interrupted by user!", t)
      case t:ErrorExecuteResponse =>
        warn(s"execute job $toString failed!", t)
        ErrorExecuteResponse("execute job failed!", t)
    }
    executeResponse match {
      case r: CompletedExecuteResponse =>
        setResultSize(0)
        transitionCompleted(r)
      case _ => logger.error("not completed")
    }
  }
}
