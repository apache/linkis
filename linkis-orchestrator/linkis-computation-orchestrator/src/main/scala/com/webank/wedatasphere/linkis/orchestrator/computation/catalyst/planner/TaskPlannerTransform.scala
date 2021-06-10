/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.planner

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.orchestrator.code.plans.ast.{CodeJob, CodeStage}
import com.webank.wedatasphere.linkis.orchestrator.code.plans.logical.{CodeLogicalUnitTask, CodeLogicalUnitTaskDesc}
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.PlannerTransform
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.{ASTContext, Job, Stage}
import com.webank.wedatasphere.linkis.orchestrator.plans.logical._
import com.webank.wedatasphere.linkis.orchestrator.plans.unit.CodeLogicalUnit

import scala.collection.mutable.ArrayBuffer

/**
 *
 *
 */
class TaskPlannerTransform extends PlannerTransform with Logging {

  private var startJobTask: JobTask = _

  def rebuildTreeNode(tmpTask: Task): Task = {
    tmpTask.getChildren.foreach(_.withNewParents(Array(tmpTask)))
    tmpTask
  }

  def buildCodeLogicTaskTree(codeLogicalUnit: CodeLogicalUnit = null, stage: Stage): Task = {
    val codeLogicalUnitTaskTmp = new CodeLogicalUnitTask(Array(), Array(buildStageTaskTree(StartStageTaskDesc(stage))))
    codeLogicalUnitTaskTmp.setTaskDesc(CodeLogicalUnitTaskDesc(stage))
    if(codeLogicalUnit != null) codeLogicalUnitTaskTmp.setCodeLogicalUnit(codeLogicalUnit)
    rebuildTreeNode(codeLogicalUnitTaskTmp)
  }

  def buildStageTaskTree(taskDesc: StageTaskDesc): Task = {
    taskDesc match {
      case endStageTask: EndStageTaskDesc => {
        val stageTaskTmp  = new StageTask(Array(), Array(buildCodeLogicTaskTree(taskDesc.stage.getJob match{
          case codeJob: CodeJob => codeJob.getCodeLogicalUnit
          case job: Job => {
            error(s"jobId:${job.getId}-----jobType:${job.getName}, job type mismatch, only support CodeJob")
            null
          }
        }, taskDesc.stage)))
        stageTaskTmp.setTaskDesc(endStageTask)
        rebuildTreeNode(stageTaskTmp)
      }
      case startStageTask: StartStageTaskDesc => {
        /** when the construction node arrives at stage-task-start
         *  check whether this stage has child nodes
         *  if true -> use the same way to build another stage tasks
         *  if false -> build or reuse job-task-start and points to the stage-task-start
         * */
        if(null == taskDesc.stage.getChildren || taskDesc.stage.getChildren.isEmpty){
          val stageTaskTmp = new StageTask(Array(),
            if(startJobTask != null) Array(startJobTask)
            else Array(buildJobTaskTree(StartJobTaskDesc(startStageTask.stage.getJob))))
          stageTaskTmp.setTaskDesc(startStageTask)
          rebuildTreeNode(stageTaskTmp)
        }else{
          val stageTaskTmp = new StageTask(Array(), buildAllStageTaskTree(taskDesc.stage.getChildren))
          stageTaskTmp.setTaskDesc(taskDesc)
          rebuildTreeNode(stageTaskTmp)
        }
      }
    }
  }

  def buildAllStageTaskTree(stages: Array[Stage]): Array[Task] = {
    val stageTasks = ArrayBuffer[Task]()
      stages.foreach(stage => {
        val stageTask = buildStageTaskTree(EndStageTaskDesc(stage))
        stageTasks += stageTask
      })
    stageTasks.toArray
  }

  def buildJobTaskTree(taskDesc: TaskDesc): Task = {
    taskDesc match {
      case startTask: StartJobTaskDesc => {
          /**
          * The end of recursion
          */
          val jobTask = new JobTask(Array(), Array())
          jobTask.setTaskDesc(startTask)
          startJobTask = jobTask
          jobTask
      }
      case endTask: EndJobTaskDesc => {
        val jobTaskTmp = new JobTask(Array(), buildAllStageTaskTree(endTask.job.getAllStages))
        jobTaskTmp.setTaskDesc(endTask)
        rebuildTreeNode(jobTaskTmp)
      }
    }
  }

  override def apply(in: Job, context: ASTContext): Task = {
    in match {
      case job: CodeJob =>
        // TODO rebuild needed:  Notice( Stages maybe have dependency relation.)
        // TODO This class should be split into two kind of transforms.
        // TODO First, two PlannerTransforms are needed: one to transform Job to JobTaskEnd, one to transform Job to StageTaskEnd.
        // TODO Second, AnalyzeTransforms are needed: one for adding a computationTask by stage for no computation strategy,
        //  one to transform Job to JobTaskStart, one to transform Job to StageTaskStart.
        buildJobTaskTree(EndJobTaskDesc(job))
      case _ => error(s"unknown job type:${in.getClass} ")
        null
    }
  }

  override def getName: String = {
    val className = getClass.getName
    if (className endsWith "$") className.dropRight(1) else className
  }
}
