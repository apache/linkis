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

package com.webank.wedatasphere.linkis.ecm.core.engineconn

import java.io.Closeable
import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.ecm.core.launch.{EngineConnLaunchRunner, EngineConnManagerEnv}
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity.EngineConnCreationDesc
import com.webank.wedatasphere.linkis.manager.label.entity.Label


trait EngineConn extends Closeable {

  def getTickedId: String

  def setTickedId(tickedId: String)

  def getServiceInstance: ServiceInstance

  def setServiceInstance(serviceInstance: ServiceInstance): Unit

  def getResource: NodeResource

  def setResource(resource: NodeResource): Unit

  def getLabels: util.List[Label[_]]

  def setLabels(labels: util.List[Label[_]]): Unit

  def getStatus: NodeStatus

  def setStatus(status: NodeStatus): Unit

  def getCreationDesc: EngineConnCreationDesc

  def setCreationDesc(desc: EngineConnCreationDesc): Unit

  def getEngineConnInfo: EngineConnInfo

  def setEngineConnInfo(engineConnInfo: EngineConnInfo): Unit

  def getEngineConnManagerEnv: EngineConnManagerEnv

  def setEngineConnManagerEnv(env: EngineConnManagerEnv): Unit

  def getEngineConnLaunchRunner: EngineConnLaunchRunner

  def setEngineConnLaunchRunner(runner: EngineConnLaunchRunner): Unit

  def setPid(pid: String): Unit

  def getPid: String

  override def close(): Unit = {}
}
