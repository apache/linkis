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
import com.webank.wedatasphere.linkis.governance.common.entity.ExecutionNodeStatus
import com.webank.wedatasphere.linkis.governance.common.protocol.task.{RequestTask, ResponseTaskStatus}
import com.webank.wedatasphere.linkis.orchestrator.ecm.entity.Mark
import com.webank.wedatasphere.linkis.scheduler.executer.{ConcurrentTaskOperateSupport, ErrorExecuteResponse, ExecutorState, SubmitResponse}



class DefaultEntranceExecutor(id: Long, mark: Mark) extends EntranceExecutor(id, mark) with ConcurrentTaskOperateSupport {


  /* private def doMethod[T](exec: String => T): T = if (engineReturns.isEmpty)
     throw new EntranceErrorException(20001, s"Engine${id} could not find a job in RUNNING state(Engine${id}找不到处于RUNNING状态的Job)")
   else exec(engineReturns(0).execId)*/


  override protected def callExecute(request: RequestTask): EngineExecuteAsynReturn = {
    val response = getEngineConnExecutor().execute(request)
    response match {
      case SubmitResponse(execId) => new EngineExecuteAsynReturn(request, getEngineConnExecutor().getServiceInstance.getInstance, execId, engineReturn => {
        info("remove execId-" + execId + " with instance " + getEngineConnExecutor().getServiceInstance)
        engineReturns -= engineReturn
      })
      case ErrorExecuteResponse(message, t) =>
        info(s"failed to submit task to engineConn,reason: $message")
        throw t
    }
  }

  override def close(): Unit = {
    if (engineReturns.nonEmpty) engineReturns.foreach { e =>
      e.notifyError(s"$toString has already been completed with state $state.")
      e.notifyStatus(ResponseTaskStatus(e.execId, ExecutionNodeStatus.Failed))
    }
  }

  override def kill(jobId: String): Boolean = {
    info(s"start to kill job $jobId")
    engineReturns.find(_.getJobId.contains(jobId)).exists(e => killExecId(e.execId))
  }

  override def killAll(): Boolean = {
    engineReturns.foreach(f => Utils.tryQuietly(killExecId(f.execId)))
    true
  }

  override def pause(jobId: String): Boolean = {
    //TODO
    true
  }


  override def pauseAll(): Boolean = {
    //TODO
    true
  }

  override def resume(jobId: String): Boolean = {
    //TODO
    true
  }

  override def resumeAll(): Boolean = {
    //TODO
    true
  }


}
