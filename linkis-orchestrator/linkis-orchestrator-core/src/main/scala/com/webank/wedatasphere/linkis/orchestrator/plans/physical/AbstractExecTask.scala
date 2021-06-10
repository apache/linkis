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
 */

package com.webank.wedatasphere.linkis.orchestrator.plans.physical

import com.webank.wedatasphere.linkis.orchestrator.plans.logical.TaskDesc

/**
  *
  *
  */
abstract class AbstractExecTask(protected var parents: Array[ExecTask],
                                protected var children: Array[ExecTask]) extends ExecTask {

  def this(){
    this(Array[ExecTask](), Array[ExecTask]())
  }
  override def withNewChildren(children: Array[ExecTask]): Unit = this.children = children

  override def withNewParents(parents: Array[ExecTask]): Unit = this.parents = parents

  override def getParents: Array[ExecTask] = parents

  override def getChildren: Array[ExecTask] = children

  private var taskDesc: TaskDesc = _

  def setTaskDesc(taskDesc: TaskDesc): Unit = {
    this.taskDesc = taskDesc
  }

  override def getTaskDesc: TaskDesc = taskDesc

  override def theSame(other: ExecTask): Boolean = {
    null != other && getId == other.getId
  }
}
