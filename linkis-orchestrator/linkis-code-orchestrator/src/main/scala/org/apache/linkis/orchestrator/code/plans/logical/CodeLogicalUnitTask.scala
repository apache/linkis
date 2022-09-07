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

package org.apache.linkis.orchestrator.code.plans.logical

import org.apache.linkis.orchestrator.plans.logical.{AbstractTask, Origin, Task}
import org.apache.linkis.orchestrator.plans.unit.CodeLogicalUnit
import org.apache.linkis.orchestrator.utils.OrchestratorIDCreator

class CodeLogicalUnitTask(private var parents: Array[Task], private var children: Array[Task])
    extends AbstractTask {

  private var codeLogicalUnit: CodeLogicalUnit = _

  private var id: String = _

  override def getOrigin: Origin = getTaskDesc.getOrigin

  override def withNewChildren(children: Array[Task]): Unit = modifyFamilyNodes(parents, children)

  override def withNewParents(parents: Array[Task]): Unit = modifyFamilyNodes(parents, children)

  private def modifyFamilyNodes(parents: Array[Task], children: Array[Task]): Unit = {
    this.parents = parents
    this.children = children
  }

  def getCodeLogicalUnit: CodeLogicalUnit = this.codeLogicalUnit

  def setCodeLogicalUnit(codeLogicalUnit: CodeLogicalUnit): Unit = this.codeLogicalUnit =
    codeLogicalUnit

  override def getId: String = {
    if (null == id) synchronized {
      if (null == id) {
        id = OrchestratorIDCreator.getLogicalTaskIDCreator.nextID("codeLogical")
      }
    }
    id
  }

  override def getParents: Array[Task] = parents

  override def getChildren: Array[Task] = children

  override protected def newNode(): Task = {
    val codeLogicalUnitTask = new CodeLogicalUnitTask(null, null)
    codeLogicalUnitTask.setTaskDesc(getTaskDesc)
    codeLogicalUnitTask.setCodeLogicalUnit(getCodeLogicalUnit)
    codeLogicalUnitTask
  }

}
