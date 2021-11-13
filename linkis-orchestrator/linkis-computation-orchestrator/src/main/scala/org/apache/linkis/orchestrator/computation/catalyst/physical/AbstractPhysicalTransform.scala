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
import org.apache.linkis.orchestrator.execution.TaskResponse
import org.apache.linkis.orchestrator.extensions.catalyst.PhysicalTransform
import org.apache.linkis.orchestrator.plans.logical.{LogicalContext, Task}
import org.apache.linkis.orchestrator.plans.physical._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * To transform logical task to executable task in common condition
 *
 */
abstract class AbstractPhysicalTransform extends PhysicalTransform with Logging{

  // TODO rebuild needed! please refer to [[TaskPlannerTransform]].
  override def apply(in: Task, context: LogicalContext): ExecTask = {
    //Find the root node of true
    in match {
      case logicalTask: Task =>
        val rootNode = searchRootLogicalTask(logicalTask)
        //Rebuild logical tree to physical
        val leafExecTasks = new ArrayBuffer[ExecTask]()
        var rootTask:ExecTask = null
        //Init root physical context
        val rootPhysicalContext = new PhysicalContextImpl(null, null){
          override def getRootTask: ExecTask = rootTask

          override def getLeafTasks: Array[ExecTask] = leafExecTasks.toArray
        }
        rootTask = rebuildToPhysicalTree(rootPhysicalContext, null, rootNode, mutable.Map[String, ExecTask](), leafExecTasks, (parentExecTasks, task) => {
          val parentPrev = Option(parentExecTasks).getOrElse(Array[ExecTask]())
          val newExecTask = doTransform()(task)
          val parent = Option(newExecTask.getParents).getOrElse(Array[ExecTask]())
          if(parent.length <= 0){
            debug(s"pad parents for execute task ${newExecTask.getId}")
            newExecTask.withNewParents(parentPrev)
          }
          newExecTask
        })
        rootTask.initialize(rootPhysicalContext)
        rootTask
      case _ => null
    }
  }

  override def getName: String = {
    //Cannot ignore inner class
    getClass.getName
  }

  /**
   * Transform function for single logical node
   * @return scala func
   */
   def doTransform(): Task => ExecTask


  /**
   * Search root node(logical task)
   * @param in task
   * @return root node
   */
  private def searchRootLogicalTask(in: Task): Task = {
    val queue = new mutable.Queue[Any]()
    queue.enqueue(in)
    while(queue.nonEmpty){
      val element = queue.dequeue()
      element match {
        case logicalNode: Task =>
          Option(logicalNode.getParents) match {
            case None =>
              return logicalNode
            case Some(parents : Array[Task]) =>
              if(parents.length <=  0) return logicalNode
              parents.foreach(queue.enqueue(_))
          }
        case _ =>
      }
    }
    null
  }

  /**
   * Main method  to build physical tree
   * @param rootPhysicalContext physical context for root node
   * @param parentExecTask parent physical task
   * @param nodeTask current logical task
   * @param branches branches of tree
   * @param transform transform function
   * @return
   */
  private def rebuildToPhysicalTree(rootPhysicalContext: PhysicalContext, parentExecTask: ExecTask, nodeTask: Task,
                                    branches: mutable.Map[String, ExecTask], leafs: ArrayBuffer[ExecTask],
                                    transform: (Array[ExecTask], Task) => ExecTask): ExecTask = {
    var recurse = true
    val parentExecTasks = if(Option(parentExecTask).isDefined){Array(parentExecTask)}else{Array[ExecTask]()}
    var nodeExecTask = if(Option(nodeTask.getParents).getOrElse(Array[Task]()).length > 1){
      recurse = false
      branches.getOrElse(nodeTask.getId, {
        recurse = true
        val execTask = Option(transform(parentExecTasks, nodeTask)).getOrElse(new UnknownExecTak)
        branches.put(nodeTask.getId, execTask)
        warn(s"meet up branch node[id:${nodeTask.getId}, name:${nodeTask.getName}] of logical node while building physical tree")
        execTask
      })
    }else{
      Option(transform(parentExecTasks, nodeTask)).getOrElse(new UnknownExecTak)
    }
    if(recurse){
      val childrenExecTask = new ArrayBuffer[ExecTask]
      Option(nodeTask.getChildren).getOrElse(Array[Task]()).foreach( childTask =>{
        val childExecTask = rebuildToPhysicalTree(rootPhysicalContext, nodeExecTask, childTask, branches, leafs, transform)
        childExecTask.initialize(rootPhysicalContext)
        childrenExecTask += childExecTask
      })
      if(childrenExecTask.length <= 0){
        //leaf node
        leafs += nodeExecTask
      }else{
        nodeExecTask.withNewChildren((childrenExecTask ++ Option(nodeExecTask.getChildren).getOrElse(Array[ExecTask]())).toArray)
      }
    }else{
      debug(s"stop to recurse, because of branch node[id:${nodeExecTask.getId}, name:${nodeExecTask.getName}] of physical node")
      val parents = Option(nodeExecTask.getParents).getOrElse(Array[ExecTask]()).toBuffer
      parents ++= parentExecTasks
      nodeExecTask.withNewParents(parents.toArray)
    }
    nodeExecTask
  }

}

/**
 * Unknown execute task
 */
class UnknownExecTak extends AbstractExecTask{

  override def canExecute: Boolean = false

  override def execute(): TaskResponse = null

  override def isLocalMode: Boolean = true

  override def getPhysicalContext: PhysicalContext = null

  override def initialize(physicalContext: PhysicalContext): Unit = {}

  override def getParents: Array[ExecTask] = null

  override def getChildren: Array[ExecTask] = null

  override protected def newNode(): ExecTask = null

  override def verboseString: String = null

  override def withNewChildren(children: Array[ExecTask]): Unit = {}

  override def withNewParents(parents: Array[ExecTask]): Unit = {}

  override def getId: String = "id"
}
