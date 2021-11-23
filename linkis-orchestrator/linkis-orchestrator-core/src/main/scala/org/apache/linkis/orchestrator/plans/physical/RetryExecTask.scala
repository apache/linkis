/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.orchestrator.plans.physical

import org.apache.linkis.orchestrator.conf.OrchestratorConfiguration
import org.apache.linkis.orchestrator.exception.{OrchestratorErrorCodeSummary, OrchestratorErrorException}
import org.apache.linkis.orchestrator.execution.TaskResponse
import org.apache.linkis.orchestrator.listener.task.TaskInfoEvent
import org.apache.linkis.orchestrator.plans.logical.TaskDesc
import org.apache.linkis.orchestrator.strategy.{ResultSetExecTask, StatusInfoExecTask}
import org.apache.linkis.orchestrator.strategy.async.AsyncExecTask
import org.apache.linkis.orchestrator.utils.OrchestratorIDCreator

/**
 *
 */
class RetryExecTask(private val originTask: ExecTask, private val age: Int = 1) extends AbstractExecTask
  with StatusInfoExecTask with ResultSetExecTask with AsyncExecTask{

  private var id: String = _

  private var physicalContext: PhysicalContext = _

  private val createTime = System.currentTimeMillis()

  def getOriginTask: ExecTask = {
    originTask
  }

  def getAge(): Int = {
    age
  }

  override def canExecute: Boolean = {
    val takenTime = System.currentTimeMillis() - createTime
    if(originTask != null) {
      originTask.canExecute && takenTime > OrchestratorConfiguration.RETRY_TASK_WAIT_TIME.getValue
    }
    else {
      throw new OrchestratorErrorException(OrchestratorErrorCodeSummary.EXECUTION_ERROR_CODE,
        s"${getIDInfo()} originTask task cannot be null" )
    }
  }

  override def execute(): TaskResponse = {
    if(canExecute){
      originTask.execute()
    }else {
      throw new OrchestratorErrorException(OrchestratorErrorCodeSummary.EXECUTION_ERROR_CODE,
        s"${getIDInfo()} task cannot be execute, task will be retried maybe not exist" +
          "(任务不允许被执行，被重热的任务可能不存在)")
    }
  }

  override def isLocalMode: Boolean = {
    if(originTask != null){
      originTask.isLocalMode
    }
    false
  }

  override def getPhysicalContext: PhysicalContext = {
    physicalContext
  }

  override def initialize(physicalContext: PhysicalContext): Unit = {
    this.physicalContext = physicalContext
  }

  override def verboseString: String = {
    if(originTask.verboseString != null){
      originTask.verboseString
    }else{
      getTaskDesc.toString
    }
  }

  override protected def newNode(): ExecTask = {
    val retryExecTask = new RetryExecTask(originTask)
    retryExecTask
  }

  override def getId: String = {
    if (null == id) synchronized {
      if (null == id) {
        id = OrchestratorIDCreator.getRetryTaskIDCreator.nextID("retry")
      }
    }
    id
  }

  override def getTaskDesc: TaskDesc = getOriginTask.getTaskDesc

  override def kill(): Unit = {
    getOriginTask match {
      case asyncExecTask: AsyncExecTask =>
        asyncExecTask.kill()
      case _ =>
    }
  }

  override def clear(isSucceed: Boolean): Unit = {
    getOriginTask match {
      case asyncExecTask: AsyncExecTask =>
        asyncExecTask.clear(isSucceed)
      case _ =>
    }
  }

  override def canDealEvent(event: TaskInfoEvent): Boolean = {
    if (null != getOriginTask ) {
      getOriginTask.getId.equals(event.execTask.getId)
    } else {
      false
    }
  }
}
