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

package com.webank.wedatasphere.linkis.orchestrator.plans.ast

import java.util

import com.webank.wedatasphere.linkis.manager.label.entity.Label

abstract class AbstractJob extends Job {

  private var labels: util.List[Label[_]] = _

  private var params: util.Map[String, Any] = _

  private var priority: Int = _

  private var executeUser: String = _

  def getParams = params

  def setParams(params: util.Map[String, Any]) = this.params = params

  def getExecuteUser: String = executeUser

  def setExecuteUser(executeUser: String) = this.executeUser = executeUser

  def getLabels: util.List[Label[_]] = labels

  def setLabels(labels: util.List[Label[_]]) = this.labels = labels

  def getPriority: Int = priority

  def setPriority(priority: Int) = this.priority = priority


}
