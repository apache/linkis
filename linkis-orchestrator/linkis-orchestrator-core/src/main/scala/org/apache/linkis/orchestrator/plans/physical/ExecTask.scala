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

package org.apache.linkis.orchestrator.plans.physical

import org.apache.linkis.orchestrator.execution.TaskResponse
import org.apache.linkis.orchestrator.plans.ast.{Job, Stage}
import org.apache.linkis.orchestrator.plans.logical.TaskDesc

/**
 */
trait ExecTask extends PhysicalOrchestration[ExecTask] {

  def canExecute: Boolean

  def execute(): TaskResponse

  def getTaskDesc: TaskDesc

  def isLocalMode: Boolean

  private var _params: Map[String, String] = Map.empty

  override def hashCode(): Int = getId.hashCode

  override def equals(obj: Any): Boolean = obj match {
    case execTask: ExecTask => execTask.getId.equals(getId)
    case _ => false
  }

  def params: Map[String, String] = _params

  def updateParams(key: String, value: String): Unit = {
    _params += (key -> value)
  }

  def getIndexValue(key: String): Option[String] = _params.get(key)

  def getIDInfo(): String = {
    val desc = getTaskDesc
    val jobID = desc.getOrigin.getASTOrchestration match {
      case job: Job =>
        job.getIDInfo()
      case stage: Stage =>
        stage.getJob.getIDInfo()
      case _ => ""
    }
    jobID + "_" + getId
  }

}
