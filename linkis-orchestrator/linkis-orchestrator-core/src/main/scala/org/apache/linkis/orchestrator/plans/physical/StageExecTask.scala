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

import org.apache.linkis.orchestrator.execution.{SucceedTaskResponse, TaskResponse}
import org.apache.linkis.orchestrator.utils.OrchestratorIDCreator

/**
 */
class StageExecTask(parents: Array[ExecTask], children: Array[ExecTask])
    extends AbstractExecTask(parents, children) {

  private var physicalContext: PhysicalContext = _

  private var id: String = _

  override def canExecute: Boolean = true

  override def execute(): TaskResponse = {
    new SucceedTaskResponse() {}
  }

  override def isLocalMode: Boolean = true

  override def initialize(physicalContext: PhysicalContext): Unit = {
    this.physicalContext = physicalContext
  }

  override def verboseString: String = ""

  override def getId: String = {
    if (null == id) synchronized {
      if (null == id) {
        id = OrchestratorIDCreator.getPhysicalStageIDCreator.nextID("stage")
      }
    }
    id
  }

  override def getPhysicalContext: PhysicalContext = physicalContext

  override protected def newNode(): ExecTask = {
    val task = new StageExecTask(null, null)
    task.setTaskDesc(getTaskDesc)
    task
  }

}
