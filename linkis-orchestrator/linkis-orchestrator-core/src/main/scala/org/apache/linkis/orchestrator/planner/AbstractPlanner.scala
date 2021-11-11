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
 
package org.apache.linkis.orchestrator.planner

import org.apache.linkis.orchestrator.extensions.catalyst.{AnalyzeFactory, AnalyzeTransform, PlannerTransform, Transform, TransformFactory}
import org.apache.linkis.orchestrator.plans.ast.{ASTContext, ASTOrchestration, Job}
import org.apache.linkis.orchestrator.plans.logical.{LogicalContext, Task}

import scala.collection.mutable

/**
  *
  *
  */
abstract class AbstractPlanner extends Planner
  with TransformFactory[Job, Task, ASTContext]
  with AnalyzeFactory[Task, LogicalContext] {

  override def plan(astPlan: ASTOrchestration[_]): Task = astPlan match {
    case job: Job =>
      debug(s"start to plan AstTree(${astPlan.getId}) to LogicalTree.")
      var task = apply(job, astPlan.getASTContext, new mutable.HashMap[Job, Task](), plannerTransforms.map{
        transform: Transform[Job, Task, ASTContext] => transform
      })
    //TODO Should LogicalContext in Planner and Optimizer be merged into one?
    // I think it's unnecessary since they are two phases of orchestration, if some transforms need it, it's ok to merge them.
      val context = createLogicalContext(task)
      task = apply(task, context, analyzeTransforms.map{
        transform: Transform[Task, Task, LogicalContext] => transform
      })
      debug(s"Finished to plan AstTree(${astPlan.getId}) to LogicalTree.")
      task
  }

  protected def createLogicalContext(task: Task): LogicalContext

  protected def plannerTransforms: Array[PlannerTransform]

  protected def analyzeTransforms: Array[AnalyzeTransform]

}
