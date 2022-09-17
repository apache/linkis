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

package org.apache.linkis.orchestrator.extensions.catalyst

import org.apache.linkis.common.utils.{ArrayUtils, Logging}
import org.apache.linkis.orchestrator.domain.TreeNode
import org.apache.linkis.orchestrator.exception.{
  OrchestratorErrorCodeSummary,
  OrchestratorErrorException
}
import org.apache.linkis.orchestrator.plans.PlanContext

import scala.collection.mutable

/**
 */
trait TransformFactory[In <: TreeNode[In], Out <: TreeNode[Out], Context <: PlanContext] {

  protected def apply(
      from: In,
      context: Context,
      inToOutMap: mutable.HashMap[In, Out],
      transforms: Array[Transform[In, Out, Context]]
  ): Out =
    inToOutMap.getOrElseUpdate(
      from, {
        var treeNode: Out = null.asInstanceOf[Out]
        for (transform <- transforms if treeNode == null) {
          treeNode = transform.apply(from, context)
        }
        if (null == treeNode) {
          throw new OrchestratorErrorException(
            OrchestratorErrorCodeSummary.OPTIMIZER_FOR_NOT_SUPPORT_ERROR_CODE,
            "Failed to transform " + from
          )
        }
        Option(from.getChildren).foreach { oldChildren =>
          val children = oldChildren.map(apply(_, context, inToOutMap, transforms))
          if (null != children && children.nonEmpty) {
            treeNode.withNewChildren(
              ArrayUtils.copyArrayWithClass[Out](children, children(0).getClass)
            )
            children.foreach { child =>
              val parents =
                ArrayUtils.copyArray[Out](child.getParents, child.getParents.length + 1)
              parents(parents.length - 1) = treeNode
              child.withNewParents(parents)
            }
          }

        }
        treeNode
      }
    )

}

trait AnalyzeFactory[In <: TreeNode[In], Context <: PlanContext] extends Logging {

  protected def apply(
      from: In,
      context: Context,
      transforms: Array[Transform[In, In, Context]]
  ): In = {
    var lastLoopTreeNode: In = from
    var count = 0
    while (true) {
      logger.debug(s"Try to analyze ${from.getName} count($count).")
      count += 1
      val newTreeNode = transforms.foldLeft(lastLoopTreeNode) { (treeNode, transform) =>
        transform.apply(treeNode, context)
      }
      if (newTreeNode.theSame(lastLoopTreeNode)) return newTreeNode
      else lastLoopTreeNode = newTreeNode
    }
    lastLoopTreeNode
  }

}
