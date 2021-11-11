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
 
package org.apache.linkis.orchestrator.computation.catalyst.physical

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.orchestrator.code.plans.logical.{CacheTask, CodeLogicalUnitTask}
import org.apache.linkis.orchestrator.computation.physical.{CacheExecTask, CodeLogicalUnitExecTask}
import org.apache.linkis.orchestrator.extensions.catalyst.PhysicalTransform
import org.apache.linkis.orchestrator.plans.logical.{CommandTask, JobTask, LogicalContext, StageTask, StartJobTaskDesc, Task}
import org.apache.linkis.orchestrator.plans.physical.{ExecTask, JobExecTask, StageExecTask}
import org.apache.linkis.orchestrator.strategy.{GatherStrategyJobExecTask, GatherStrategyStageInfoExecTask}

/**
 * In compute condition
 *
 */
abstract class ComputePhysicalTransform extends PhysicalTransform {

  override def getName: String = getClass.getName
}

class JobExecTaskTransform extends  ComputePhysicalTransform with Logging {

  override def apply(in: Task, context: LogicalContext): ExecTask = in match {
    case jobTask: JobTask =>
      val jobExecTask = new GatherStrategyJobExecTask(Array[ExecTask](), Array[ExecTask]())
      jobExecTask.setTaskDesc(jobTask.getTaskDesc)
      jobExecTask
    case _ =>
      null
  }

}

class StageExecTaskTransform extends  ComputePhysicalTransform with Logging {

  override def apply(in: Task, context: LogicalContext): ExecTask = in match {
    case stageTask: StageTask =>
      val stageExecTask = new GatherStrategyStageInfoExecTask(Array[ExecTask](), Array[ExecTask]())
      stageExecTask.setTaskDesc(stageTask.getTaskDesc)
      stageExecTask
    case _ =>
      null
  }


}

class CacheExecTaskTransform extends  ComputePhysicalTransform with Logging {

  override def apply(in: Task, context: LogicalContext): ExecTask = in match {
    case cacheTask: CacheTask  =>
      val cacheExecTask = new CacheExecTask(Array[ExecTask](), Array[ExecTask]())
      val realExecTask = new CodeLogicalUnitExecTask(Array[ExecTask](), Array[ExecTask]())
      realExecTask.setCodeLogicalUnit(cacheTask.getRealTask().getCodeLogicalUnit)
      realExecTask.setTaskDesc(cacheTask.getRealTask().getTaskDesc)
      cacheExecTask.setRealExecTask(realExecTask)
      cacheExecTask.setTaskDesc(cacheTask.getTaskDesc)
      cacheExecTask
    case _ =>
      null
  }

}


class CodeExecTaskTransform extends  ComputePhysicalTransform with Logging {

  override def apply(in: Task, context: LogicalContext): ExecTask = in match {
    case codeUnitTask: CodeLogicalUnitTask =>
      val codeUnitExecTask  = new CodeLogicalUnitExecTask(Array[ExecTask](), Array[ExecTask]())
      //set code unit
      codeUnitExecTask.setCodeLogicalUnit(codeUnitTask.getCodeLogicalUnit)
      codeUnitExecTask.setTaskDesc(codeUnitTask.getTaskDesc)
      codeUnitExecTask
    case _ =>
      null
  }

}

object ComputePhysicalTransform{
  def main(args: Array[String]): Unit = {
    /*val jobStartExec = new JobTask(null, null)
    jobStartExec.setTaskDesc(new StartJobTaskDesc(null))
    val stage1 = new StageTask(Array(jobStartExec), null)
    val stage2 = new StageTask(Array(jobStartExec), null)
    jobStartExec.withNewChildren(Array(stage1, stage2))
    val jobEndTask = new JobTask(Array(stage1, stage2), null)
    stage1.withNewChildren(Array(jobEndTask))
    stage2.withNewChildren(Array(jobEndTask))
    val transform = new ComputePhysicalTransform()
    val result = transform.apply(stage1, null)
    println(result)
    println(result.getChildren.length)*/
  }
}