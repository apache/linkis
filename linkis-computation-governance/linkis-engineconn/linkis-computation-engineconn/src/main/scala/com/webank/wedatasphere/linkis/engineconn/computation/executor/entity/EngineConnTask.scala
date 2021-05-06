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

package com.webank.wedatasphere.linkis.engineconn.computation.executor.entity

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.governance.common.entity.ExecutionNodeStatus
import com.webank.wedatasphere.linkis.manager.label.entity.Label


trait EngineConnTask {

  def getTaskId: String

  def isTaskSupportRetry: Boolean

  def getCode: String

  def setCode(code: String): Unit

  def getProperties: util.Map[String, Object]

  def setProperties(properties: util.Map[String, Object])

  def data(key: String, value: Object): Unit

  def getStatus: ExecutionNodeStatus

  def setStatus(taskStatus: ExecutionNodeStatus): Unit


  def getLables: Array[Label[_]]

  def setLabels(labels: Array[Label[_]]): Unit

  def getCallbackServiceInstance(): ServiceInstance

  def setCallbackServiceInstance(serviceInstance: ServiceInstance): Unit
}
