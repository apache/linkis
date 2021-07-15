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

package com.webank.wedatasphere.linkis.orchestrator.computation.execute

import com.webank.wedatasphere.linkis.orchestrator.ecm.entity.Mark
import com.webank.wedatasphere.linkis.orchestrator.ecm.service.EngineConnExecutor
import com.webank.wedatasphere.linkis.orchestrator.plans.physical.ExecTask

/**
  *
  *
  */
class CodeExecTaskExecutor(engineConnExecutor: EngineConnExecutor, execTask: ExecTask, mark: Mark) {

  private var engineConnTaskId: String = _

  def getEngineConnExecutor: EngineConnExecutor = engineConnExecutor

  def getExecTaskId: String = execTask.getId

  def getExecTask: ExecTask = execTask


  def getEngineConnTaskId: String = engineConnTaskId

  def setEngineConnTaskId(engineConnTaskId: String ) = this.engineConnTaskId = engineConnTaskId

  def getMark: Mark = mark

  override def toString: String = s"engineConn $engineConnExecutor execTask ${execTask.getIDInfo()} mark ${mark.getMarkId()} engineConnTaskId $engineConnTaskId"

}
