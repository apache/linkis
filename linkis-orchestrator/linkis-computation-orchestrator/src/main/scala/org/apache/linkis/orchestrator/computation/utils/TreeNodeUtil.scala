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

package org.apache.linkis.orchestrator.computation.utils

import org.apache.linkis.orchestrator.exception.{
  OrchestratorErrorCodeSummary,
  OrchestratorErrorException
}
import org.apache.linkis.orchestrator.execution.{
  AsyncTaskResponse,
  FailedTaskResponse,
  TaskResponse
}
import org.apache.linkis.orchestrator.plans.physical.ExecTask
import org.apache.linkis.orchestrator.strategy.{ExecTaskStatusInfo, StatusInfoExecTask}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

// TODO 考虑TreeNode修改的并发操作问题
object TreeNodeUtil {

  // private method, only support to check adjacent node
  private def bloodCheck(parentNode: ExecTask, childNode: ExecTask): Unit = {
    if (parentNode != null && childNode != null) {
      val children = parentNode.getChildren
      if (children == null || !children.contains(childNode)) {
        throw new OrchestratorErrorException(
          OrchestratorErrorCodeSummary.JOB_REQUEST_PARAM_ILLEGAL_ERROR_CODE,
          "the parent node does not contain the child node(该父节点不直接包含该子节点)"
        )
      }
      val parents = childNode.getParents
      if (parents == null || !parents.contains(parentNode)) {
        throw new OrchestratorErrorException(
          OrchestratorErrorCodeSummary.JOB_REQUEST_PARAM_ILLEGAL_ERROR_CODE,
          "the child node does not belong the parent node(该子节点不直接属于该父节点)"
        )
      }
    } else {
      throw new OrchestratorErrorException(
        OrchestratorErrorCodeSummary.JOB_REQUEST_PARAM_ILLEGAL_ERROR_CODE,
        "parameter is null, please check again"
      )
    }
  }

  def insertNode(parentNode: ExecTask, childNode: ExecTask, insertNode: ExecTask): Unit = {
    bloodCheck(parentNode, childNode)
    pruningTreeNode(parentNode, childNode)
    relateParentWithChild(parentNode, insertNode)
    relateParentWithChild(insertNode, childNode)
  }

  def pruningTreeNode(parentNode: ExecTask, childNode: ExecTask): Unit = {
    bloodCheck(parentNode, childNode)
    // Todo: pruning legitimacy check
    val children = parentNode.getChildren
    val newChildren = children.filter(_ != childNode)
    parentNode.withNewChildren(newChildren)
    val parents = childNode.getParents
    val newParents = parents.filter(_ != parentNode)
    childNode.withNewParents(newParents)
  }

  def relateParentWithChild(parentNode: ExecTask, childNode: ExecTask): Unit = {
    if (parentNode != null && childNode != null) {
      val buffer = new ArrayBuffer[ExecTask]()
      var parents = childNode.getParents
      buffer ++= parents
      buffer += parentNode
      childNode.withNewParents(buffer.toArray)
      var children = parentNode.getChildren
      buffer.clear()
      buffer ++= children
      buffer += childNode
      parentNode.withNewChildren(buffer.toArray)
    }
  }

  def deleteNode(node: ExecTask): Unit = {
    if (node.getParents != null) {
      node.getParents.foreach(parent => {
        pruningTreeNode(parent, node)
      })
    }
    if (node.getChildren != null) {
      node.getChildren.foreach(child => {
        pruningTreeNode(node, child)
      })
    }
  }

  def replaceNode(currentNode: ExecTask, newNode: ExecTask): Unit = {
    if (currentNode != null) {
      val children = currentNode.getChildren.clone()
      val parents = currentNode.getParents.clone()
      deleteNode(currentNode)
      children.foreach(child => {
        relateParentWithChild(newNode, child)
      })
      parents.foreach(parent => {
        relateParentWithChild(parent, newNode)
      })
    }
  }

  private def recursionSearch(
      node: ExecTask,
      statusInfoMap: mutable.Map[String, ExecTaskStatusInfo],
      failedTasks: ArrayBuffer[ExecTask]
  ): Unit = {
    if (node != null) {
      val status = statusInfoMap.get(node.getId).getOrElse(null)
      if (status != null) {
        status.taskResponse match {
          case response: FailedTaskResponse =>
            failedTasks += node
          case _ =>
        }
      }
    }
    val children = node.getChildren
    if (children != null) {
      children.foreach(child => recursionSearch(child, statusInfoMap, failedTasks))
    }
  }

  def getAllFailedTaskNode(rootNode: ExecTask): Array[ExecTask] = {
    val context = rootNode.getPhysicalContext
    val statusInfoMap = if (null != context.get(StatusInfoExecTask.STATUS_INFO_MAP_KEY)) {
      context
        .get(StatusInfoExecTask.STATUS_INFO_MAP_KEY)
        .asInstanceOf[mutable.Map[String, ExecTaskStatusInfo]]
    } else {
      new mutable.HashMap[String, ExecTaskStatusInfo]()
    }
    val failedTasks = new ArrayBuffer[ExecTask]()
    recursionSearch(rootNode, statusInfoMap, failedTasks)
    failedTasks.toArray
  }

  def getTaskResponse(task: ExecTask): TaskResponse = {
    val context = task.getPhysicalContext
    if (context != null) {
      val statusInfoMap = if (null != context.get(StatusInfoExecTask.STATUS_INFO_MAP_KEY)) {
        context
          .get(StatusInfoExecTask.STATUS_INFO_MAP_KEY)
          .asInstanceOf[mutable.Map[String, ExecTaskStatusInfo]]
      } else {
        new mutable.HashMap[String, ExecTaskStatusInfo]()
      }
      val taskStatusInfo = statusInfoMap.get(task.getId).getOrElse(null)
      if (taskStatusInfo != null) {
        taskStatusInfo.taskResponse
      } else {
        null
      }
    } else {
      null
    }
  }

  def removeTaskResponse(task: ExecTask): Option[ExecTaskStatusInfo] = {
    val context = task.getPhysicalContext
    if (context != null) {
      val statusInfoMap = if (null != context.get(StatusInfoExecTask.STATUS_INFO_MAP_KEY)) {
        context
          .get(StatusInfoExecTask.STATUS_INFO_MAP_KEY)
          .asInstanceOf[mutable.Map[String, ExecTaskStatusInfo]]
      } else {
        new mutable.HashMap[String, ExecTaskStatusInfo]()
      }
      statusInfoMap.remove(task.getId)
    } else {
      None
    }
  }

}
