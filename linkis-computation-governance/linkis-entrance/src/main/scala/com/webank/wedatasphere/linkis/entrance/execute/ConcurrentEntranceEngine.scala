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
import com.webank.wedatasphere.linkis.protocol.engine._
import com.webank.wedatasphere.linkis.scheduler.executer.{ConcurrentTaskOperateSupport, ExecutorState}

/**
  * Created by enjoyyin on 2018/10/30.
  */
class ConcurrentEntranceEngine(id: Long) extends EntranceEngine(id) with ConcurrentTaskOperateSupport {

  override protected def callExecute(request: RequestTask): EngineExecuteAsynReturn = whenAvailable {
    transition(ExecutorState.Busy)
    val response = sender.ask(request)
    response match {
      case ResponseTaskExecute(execId) =>
        new EngineExecuteAsynReturn(request, getModuleInstance.getInstance, execId, engineReturn => {
          engineReturns -= engineReturn
          if(engineReturns.isEmpty) engineReturns synchronized {
            if(engineReturns.isEmpty) transition(ExecutorState.Idle)
          }
        })
    }
  }

  override def close(): Unit =  if(engineReturns.nonEmpty && ExecutorState.isAvailable(state)) killAll()

  override def kill(jobId: String): Boolean =
    engineReturns.find(_.getJobId.contains(jobId)).exists(e => killExecId(e.execId))

  override def killAll(): Boolean = {
    engineReturns.foreach(f => Utils.tryQuietly(killExecId(f.execId)))
    true
  }

  override def pause(jobId: String): Boolean = {
    sender.send(RequestTaskPause(jobId))
    true
  }

  override def pauseAll(): Boolean = {
    engineReturns.foreach(f => Utils.tryQuietly(pause(f.execId)))
    true
  }

  override def resume(jobId: String): Boolean = {
    sender.send(RequestTaskResume(jobId))
    true
  }

  override def resumeAll(): Boolean = {
    engineReturns.foreach(f => Utils.tryQuietly(resume(f.execId)))
    true
  }
}
