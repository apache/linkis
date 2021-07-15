/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.storage.io.orchestrator

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.orchestrator.code.plans.ast.CodeJob
import com.webank.wedatasphere.linkis.orchestrator.code.plans.logical.{CodeLogicalUnitTask, CodeLogicalUnitTaskDesc}
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.PlannerTransform
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.{ASTContext, Job}
import com.webank.wedatasphere.linkis.orchestrator.plans.logical.{EndJobTaskDesc, JobTask, Task}
import com.webank.wedatasphere.linkis.orchestrator.plans.unit.CodeLogicalUnit

class IOTaskPlannerTransform extends PlannerTransform with Logging{

  override def apply(in: Job, context: ASTContext): Task = {
    in match {
      case job: CodeJob =>
        val taskDesc = EndJobTaskDesc(job)
        val jobTaskTmp = new JobTask(Array(), Array(buildCodeLogicTaskTree(job.getCodeLogicalUnit, job)))
        jobTaskTmp.setTaskDesc(taskDesc)
        rebuildTreeNode(jobTaskTmp)
      case _ => error(s"unknown job type:${in.getClass} ")
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
    if(codeLogicalUnit != null) codeLogicalUnitTaskTmp.setCodeLogicalUnit(codeLogicalUnit)
    //rebuildTreeNode(codeLogicalUnitTaskTmp)
    codeLogicalUnitTaskTmp
  }



  override def getName: String = "IOTaskPlannerTransform"
}
