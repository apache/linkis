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
 
package org.apache.linkis.orchestrator.execution

import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.orchestrator.domain.Node
import org.apache.linkis.orchestrator.execution.AsyncTaskResponse.NotifyListener
import org.apache.linkis.orchestrator.plans.physical.ExecTask

/**
  *
  */
trait ExecutionTask extends Node {

  def getMaxParallelism: Int

  def waitForCompleted(): Unit

  def notifyMe(listener: NotifyListener): Unit

  def getRootExecTask: ExecTask

  def getResponse: TaskResponse

  def getStatus: ExecutionNodeStatus

  def transientStatus(status: ExecutionNodeStatus): Unit

  def markCompleted(taskCompletedTaskResponse: CompletedTaskResponse)

}