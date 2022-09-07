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

package org.apache.linkis.orchestrator.execution

import org.apache.linkis.orchestrator.listener.OrchestratorSyncListener
import org.apache.linkis.orchestrator.plans.physical.ExecTask

/**
 */
trait TaskManager extends OrchestratorSyncListener {

  def putExecTask(task: ExecTask): ExecutionTask

  def getRunningTask(executionTaskId: String): Array[ExecTaskRunner]

  def getRunningTask(task: ExecTask): Array[ExecTaskRunner]

  def getCompletedTasks(executionTaskId: String): Array[ExecTaskRunner]

  def getCompletedTasks(task: ExecTask): Array[ExecTaskRunner]

  def getRunnableTasks: Array[ExecTaskRunner]

  def taskRunnableTasks(execTaskRunners: Array[ExecTaskRunner]): Array[ExecTaskRunner]

  def addCompletedTask(task: ExecTaskRunner): Unit

  def pollCompletedExecutionTasks: Array[ExecutionTask]

}
