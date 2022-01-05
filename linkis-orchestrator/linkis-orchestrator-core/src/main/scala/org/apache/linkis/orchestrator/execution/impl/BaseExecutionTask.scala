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
 
package org.apache.linkis.orchestrator.execution.impl

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.orchestrator.exception.{OrchestratorErrorCodeSummary, OrchestratorErrorException}
import org.apache.linkis.orchestrator.execution.AsyncTaskResponse.NotifyListener
import org.apache.linkis.orchestrator.execution.{CompletedTaskResponse, ExecutionTask, SucceedTaskResponse, TaskResponse}
import org.apache.linkis.orchestrator.plans.physical.ExecTask
import org.apache.linkis.orchestrator.utils.OrchestratorIDCreator

import scala.collection.mutable.ArrayBuffer

/**
  *
  *
  */
class BaseExecutionTask(maxParallelism: Int, rootExecTask: ExecTask) extends ExecutionTask with Logging{

  private val waitLock = new Array[Byte](0)

  private val listeners = ArrayBuffer[NotifyListener]()

  private var status: ExecutionNodeStatus = ExecutionNodeStatus.Inited

  private var response: TaskResponse = _

  private var id: String = _

  override def getMaxParallelism: Int = maxParallelism

  override def waitForCompleted(): Unit = {
    if(ExecutionNodeStatus.isCompleted(getStatus)) return
    waitLock synchronized {
      while(!ExecutionNodeStatus.isCompleted(getStatus)) waitLock.wait()
    }
  }

  override def getStatus: ExecutionNodeStatus = status

  override def notifyMe(listener: NotifyListener): Unit = {
    listeners += listener
  }

  override def getRootExecTask: ExecTask = rootExecTask

  override def transientStatus(status: ExecutionNodeStatus): Unit = {
    if(status.ordinal() < this.status.ordinal() && status != ExecutionNodeStatus.WaitForRetry)
      throw new OrchestratorErrorException(OrchestratorErrorCodeSummary.EXECUTION_FOR_EXECUTION_ERROR_CODE, s"Task status flip error! Cause: Failed to flip from ${this.status} to $status.")//抛异常
    info(s"$getId change status ${this.status} => $status.")
    beforeStatusChanged(this.status, status)
    val oldStatus = this.status
    this.status = status
    afterStatusChanged(oldStatus, status)
  }

  // status 完成后返回执行响应
  def afterStatusChanged(fromStatus: ExecutionNodeStatus, toStatus: ExecutionNodeStatus): Unit = {
    if(ExecutionNodeStatus.isCompleted(toStatus)) {
      Utils.tryAndWarn(listeners.foreach(listener => listener(getResponse)))
      waitLock synchronized waitLock.notify()
    }
  }

  override def getResponse: TaskResponse = this.response

  def beforeStatusChanged(fromStatus: ExecutionNodeStatus, toStatus: ExecutionNodeStatus):Unit = {}

  override def getId: String = {
    if (null == id) synchronized {
      if (null == id) {
        id = OrchestratorIDCreator.getExecutionIDCreator.nextID("execution")
      }
    }
    id
  }

  override def equals(obj: Any): Boolean = {
    if (null != obj && obj.isInstanceOf[ExecutionTask]) {
      obj.asInstanceOf[ExecutionTask].getId == this.getId
    } else {
      false
    }
  }

  override def hashCode(): Int = getId.hashCode

  override def markCompleted(taskCompletedTaskResponse: CompletedTaskResponse): Unit = {

    this.response = taskCompletedTaskResponse
    taskCompletedTaskResponse match {
      case failedTaskResponse: DefaultFailedTaskResponse =>
        transientStatus(ExecutionNodeStatus.Failed)
      case succeedTaskResponse: SucceedTaskResponse =>
        transientStatus(ExecutionNodeStatus.Succeed)
    }
    info(s"Finished to ExecutionTask(${getId}) with status $getStatus")
  }
}
