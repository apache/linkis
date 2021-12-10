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
import java.util

import org.apache.linkis.common.listener.Event
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.orchestrator.exception.OrchestratorErrorCodeSummary
import org.apache.linkis.orchestrator.execution.impl.DefaultFailedTaskResponse
import org.apache.linkis.orchestrator.execution.{CompletedTaskResponse, SucceedTaskResponse, TaskResponse}
import org.apache.linkis.orchestrator.listener._
import org.apache.linkis.orchestrator.listener.task.{RootTaskResponseEvent, TaskLogEvent, TaskProgressEvent}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  *
  *
  */
class PhysicalContextImpl(private var rootTask: ExecTask,private var leafTasks: Array[ExecTask]) extends PhysicalContext {

  private var syncListenerBus: OrchestratorSyncListenerBus = _ //Orchestartor.getOrchestratorSyncListenerBus

  private var asyncListenerBus: OrchestratorAsyncListenerBus = _ //OrchestratorListenerBusContext.getListenerBusContext().getOrchestratorAsyncListenerBus

  private var executionNodeStatus: ExecutionNodeStatus = ExecutionNodeStatus.Inited

  private var response: TaskResponse = _

  private val context: java.util.Map[String, Any] = new util.concurrent.ConcurrentHashMap[String, Any]()

  private var rootPhysicalContext: PhysicalContext = _

  def this(rootPhysicalContext: PhysicalContext) = {
    this(rootPhysicalContext.getRootTask, rootPhysicalContext.getLeafTasks)
    this.rootPhysicalContext = rootPhysicalContext
  }

  override def isCompleted: Boolean = ExecutionNodeStatus.isCompleted(executionNodeStatus)

  override def markFailed(errorMsg: String, cause: Throwable): Unit = {
    this.executionNodeStatus = ExecutionNodeStatus.Failed
    val failedResponse = new DefaultFailedTaskResponse(errorMsg, OrchestratorErrorCodeSummary.EXECUTION_ERROR_CODE, cause)
    this.response = failedResponse
    syncListenerBus.postToAll(RootTaskResponseEvent(getRootTask, failedResponse))
  }

  override def markSucceed(response: TaskResponse): Unit = {
    this.executionNodeStatus = ExecutionNodeStatus.Succeed
    this.response = response
    response match {
      case completedResponse: CompletedTaskResponse =>
        syncListenerBus.postToAll(RootTaskResponseEvent(getRootTask, completedResponse))
      case _ =>
        syncListenerBus.postToAll(RootTaskResponseEvent(getRootTask, new SucceedTaskResponse {}))
    }
  }

  override def broadcastAsyncEvent(event: Event): Unit = event match {
    case orchestratorAsyncEvent: OrchestratorAsyncEvent =>
    asyncListenerBus.post(orchestratorAsyncEvent)
    case _ =>
  }

  override def broadcastToAll(event: Event): Unit = {
    broadcastSyncEvent(event)
  }

  override def broadcastSyncEvent(event: Event): Unit = {
    event match {
      case orchestratorSyncEvent: OrchestratorSyncEvent =>
        syncListenerBus.postToAll(orchestratorSyncEvent)
      case _ =>
    }
  }

  /**
   * Check if the executive task belongs to the physical tree
   * @param execTask executive task
   * @return
   */
  override def belongsTo(execTask: ExecTask): Boolean = {

    Option(rootTask) match {
      case Some(task) =>
        val branches = ListBuffer[String]()
        val traversableQueue = new mutable.Queue[ExecTask]()
        traversableQueue.enqueue(task)
        while(traversableQueue.nonEmpty){
          val nodeTask = traversableQueue.dequeue()
          if(nodeTask.theSame(execTask)){
            return true
          }
          val parent = Option(nodeTask.getParents).getOrElse(Array[ExecTask]())
          val branch = !branches.contains(nodeTask.getId)
          if (parent.length < 1 || (parent.length > 1 && branch)) {
            Option(nodeTask.getChildren).getOrElse(Array[ExecTask]())
              .foreach(traversableQueue.enqueue(_))
            if(branch){branches += nodeTask.getId}
          }
         /* parent.length match{
            case v if v <= 1 || (v > 1  && branch ) =>
              Option(nodeTask.getChildren).getOrElse(Array[ExecTask]())
                .foreach(traversableQueue.enqueue(_))
              if(branch){branches += nodeTask.getId}
          }*/
        }
        false
      case None => false
    }
  }

  override def getRootTask: ExecTask = {
    if(Option(rootTask).isEmpty && Option(rootPhysicalContext).isDefined){
      rootPhysicalContext.getRootTask
    }else{
      rootTask
    }
  }

  override def getLeafTasks: Array[ExecTask] = {
    if(Option(leafTasks).isEmpty && Option(rootPhysicalContext).isDefined){
      rootPhysicalContext.getLeafTasks
    }else{
      leafTasks
    }
  }

  override def get(key: String): Any = {
    context.get(key)
  }

  override def getOption(key: String): Option[Any] = {
    Some(context.get(key))
  }

  override def orElse(key: String, defaultValue: Any): Option[Any] = {
    Some(getOrElse(key, defaultValue))
  }

  override def getOrElse(key: String, defaultValue: Any): Any = {
    context.getOrDefault(key, defaultValue)
  }

  override def orElsePut(key: String, defaultValue: Any): Option[Any] = {
    Some(getOrElsePut(key, defaultValue))
  }

  override def getOrElsePut(key: String, defaultValue: Any): Any = synchronized {
    if (exists(key)) {
      context.get(key)
    } else {
      context.put(key, defaultValue)
      defaultValue
    }
  }

  override def exists(key: String): Boolean = {
    context.containsKey(key)
  }

  override def set(key: String, value: Any): Unit = {
    context.put(key, value)
  }

  override def pushLog(taskLogEvent: TaskLogEvent): Unit = {
    broadcastAsyncEvent(taskLogEvent)
  }

  override def pushProgress(taskProgressEvent: TaskProgressEvent): Unit = {
    broadcastAsyncEvent(taskProgressEvent)
  }

  def setAsyncBus(asyncListenerBus: OrchestratorAsyncListenerBus): Unit = {
    this.asyncListenerBus = asyncListenerBus
  }

  def setSyncBus(syncListenerBus: OrchestratorSyncListenerBus): Unit = {
    this.syncListenerBus = syncListenerBus
  }

}
