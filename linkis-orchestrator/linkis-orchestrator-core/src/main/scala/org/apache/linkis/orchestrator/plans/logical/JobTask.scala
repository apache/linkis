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

package org.apache.linkis.orchestrator.plans.logical

import org.apache.linkis.orchestrator.plans.ast.Job
import org.apache.linkis.orchestrator.utils.OrchestratorIDCreator

/**
 */
class JobTask(protected var parents: Array[Task], protected var children: Array[Task])
    extends AbstractTask {

  private var id: String = _

  override def getOrigin: Origin = getTaskDesc.getOrigin

  override def withNewChildren(children: Array[Task]): Unit = modifyFamilyNodes(parents, children)

  override def withNewParents(parents: Array[Task]): Unit = modifyFamilyNodes(parents, children)

  private def modifyFamilyNodes(parents: Array[Task], children: Array[Task]): Unit = {
    this.parents = parents
    this.children = children
  }

  override def theSame(other: Task): Boolean = if (super.equals(other)) true
  else if (other == null) false
  else {
    other match {
      case jobTask: JobTask =>
        jobTask.getParents.sameElements(parents) && jobTask.getChildren.sameElements(
          children
        ) && jobTask.getTaskDesc == getTaskDesc
      case _ => false
    }
  }

  override def getId: String = {
    if (null == id) synchronized {
      if (null == id) {
        id = OrchestratorIDCreator.getLogicalJobIDCreator.nextID("logicalJob")
      }
    }
    id
  }

  override def getParents: Array[Task] = parents

  override def getChildren: Array[Task] = children

  override protected def newNode(): Task = {
    val jobTask = new JobTask(null, null)
    jobTask.setTaskDesc(getTaskDesc)
    jobTask
  }

}

trait JobTaskDesc extends TaskDesc {
  val job: Job
  val position: Int
  private val origin = Origin(job, position)
  override def getOrigin: Origin = origin
}

case class StartJobTaskDesc(override val job: Job) extends JobTaskDesc {
  override val position: Int = 0
  override def copy(): StartJobTaskDesc = StartJobTaskDesc(job)
}

case class EndJobTaskDesc(override val job: Job) extends JobTaskDesc {
  override val position: Int = 1
  override def copy(): EndJobTaskDesc = EndJobTaskDesc(job)
}
