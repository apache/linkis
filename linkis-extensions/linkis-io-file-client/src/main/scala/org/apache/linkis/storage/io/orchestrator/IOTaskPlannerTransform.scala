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

package org.apache.linkis.storage.io.orchestrator

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.orchestrator.code.plans.ast.CodeJob
import org.apache.linkis.orchestrator.code.plans.logical.{
  CodeLogicalUnitTask,
  CodeLogicalUnitTaskDesc
}
import org.apache.linkis.orchestrator.extensions.catalyst.PlannerTransform
import org.apache.linkis.orchestrator.plans.ast.{ASTContext, Job}
import org.apache.linkis.orchestrator.plans.logical.{EndJobTaskDesc, JobTask, Task}
import org.apache.linkis.orchestrator.plans.unit.CodeLogicalUnit

class IOTaskPlannerTransform extends PlannerTransform with Logging {

  override def apply(in: Job, context: ASTContext): Task = {
    in match {
      case job: CodeJob =>
        val taskDesc = EndJobTaskDesc(job)
        val jobTaskTmp =
          new JobTask(Array(), Array(buildCodeLogicTaskTree(job.getCodeLogicalUnit, job)))
        jobTaskTmp.setTaskDesc(taskDesc)
        rebuildTreeNode(jobTaskTmp)
      case _ =>
        logger.error(s"unknown job type:${in.getClass} ")
        null
    }
  }

  def rebuildTreeNode(tmpTask: Task): Task = {
    tmpTask.getChildren.foreach(_.withNewParents(Array(tmpTask)))
    tmpTask
  }

  def buildCodeLogicTaskTree(codeLogicalUnit: CodeLogicalUnit, job: Job): Task = {
    val codeLogicalUnitTaskTmp = new CodeLogicalUnitTask(Array(), Array())
    codeLogicalUnitTaskTmp.setTaskDesc(CodeLogicalUnitTaskDesc(job))
    if (codeLogicalUnit != null) codeLogicalUnitTaskTmp.setCodeLogicalUnit(codeLogicalUnit)
    // rebuildTreeNode(codeLogicalUnitTaskTmp)
    codeLogicalUnitTaskTmp
  }

  override def getName: String = "IOTaskPlannerTransform"
}
