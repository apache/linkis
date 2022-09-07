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

package org.apache.linkis.orchestrator.optimizer

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.orchestrator.extensions.catalyst.{
  AnalyzeFactory,
  OptimizerTransform,
  PhysicalTransform,
  Transform,
  TransformFactory
}
import org.apache.linkis.orchestrator.plans.logical.{LogicalContext, Task}
import org.apache.linkis.orchestrator.plans.physical.{
  ExecTask,
  PhysicalContext,
  PhysicalContextImpl
}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 */
abstract class AbstractOptimizer
    extends Optimizer
    with TransformFactory[Task, ExecTask, LogicalContext]
    with AnalyzeFactory[Task, LogicalContext]
    with Logging {

  override def optimize(task: Task): ExecTask = {
    val context = createLogicalContext(task)
    // 优化
    logger.debug(s"Start to optimize LogicalTree(${task.getId}).")
    val optimizedTask = apply(
      task,
      context,
      optimizerTransforms.map { transform: Transform[Task, Task, LogicalContext] =>
        transform
      }
    )
    logger.debug(s"Finished to optimize LogicalTree(${task.getId}).")
    // 物化
    logger.debug(s"Start to transform LogicalTree(${task.getId}) to PhysicalTree.")
    val execTask = apply(
      optimizedTask,
      context,
      new mutable.HashMap[Task, ExecTask],
      physicalTransforms.map { transform: Transform[Task, ExecTask, LogicalContext] =>
        transform
      }
    )
    val leafNodes = new ArrayBuffer[ExecTask]()
    findLeafNode(execTask, leafNodes)
    val physicalContext = createPhysicalContext(execTask, leafNodes.toArray)
    initTreePhysicalContext(execTask, physicalContext)
    logger.debug(s"Finished to transform LogicalTree(${task.getId}) to PhysicalTree.")
    execTask
  }

  private def findLeafNode(execTask: ExecTask, leafNodes: ArrayBuffer[ExecTask]): Unit = {
    if (null != execTask.getChildren && execTask.getChildren.length > 0) {
      execTask.getChildren.foreach(findLeafNode(_, leafNodes))
    } else {
      leafNodes += execTask
    }
  }

  private def initTreePhysicalContext(
      execTask: ExecTask,
      physicalContext: PhysicalContext
  ): Unit = {
    execTask.initialize(physicalContext)
    if (null != execTask.getChildren) {
      execTask.getChildren.foreach(initTreePhysicalContext(_, physicalContext))
    }
  }

  protected def createPhysicalContext(
      execTask: ExecTask,
      leafNodes: Array[ExecTask]
  ): PhysicalContext

  protected def createLogicalContext(task: Task): LogicalContext

  protected def optimizerTransforms: Array[OptimizerTransform]

  protected def physicalTransforms: Array[PhysicalTransform]
}
