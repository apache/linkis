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
 
package org.apache.linkis.ecm.server.engineConn

import java.util

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.ecm.core.engineconn.{EngineConn, EngineConnInfo}
import org.apache.linkis.ecm.core.launch.{EngineConnLaunchRunner, EngineConnManagerEnv}
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.resource.NodeResource
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnCreationDesc
import org.apache.linkis.manager.label.entity.Label


class DefaultEngineConn extends EngineConn {

  /**
   * starting :初始状态
   * success:成功启动并且安全退出
   * Failed：启动失败
   * Running：容器运行中
   */
  @volatile private var status: NodeStatus = _

  private var tickedId: String = _

  private var resource: NodeResource = _

  private var labels: util.List[Label[_]] = _

  private var engineConnCreationDesc: EngineConnCreationDesc = _

  private var engineConnInfo: EngineConnInfo = _

  private var ecmEnv: EngineConnManagerEnv = _

  private var engineConnLaunchRunner: EngineConnLaunchRunner = _

  private var instance: ServiceInstance = _

  private var pid: String = _

  override def getResource: NodeResource = resource

  override def setResource(resource: NodeResource): Unit = this.resource = resource

  override def getLabels: util.List[Label[_]] = labels

  override def setLabels(labels: util.List[Label[_]]): Unit = this.labels = labels

  override def getStatus: NodeStatus = status

  override def getCreationDesc: EngineConnCreationDesc = engineConnCreationDesc

  override def getEngineConnInfo: EngineConnInfo = engineConnInfo

  override def getEngineConnLaunchRunner: EngineConnLaunchRunner = engineConnLaunchRunner

  override def setEngineConnLaunchRunner(runner: EngineConnLaunchRunner): Unit = this.engineConnLaunchRunner = runner

  override def setEngineConnInfo(engineConnInfo: EngineConnInfo): Unit = this.engineConnInfo = engineConnInfo

  override def getTickedId: String = tickedId

  override def setTickedId(tickedId: String): Unit = this.tickedId = tickedId

  override def setStatus(status: NodeStatus): Unit = this.status = status

  override def setCreationDesc(engineConnCreationDesc: EngineConnCreationDesc): Unit = this.engineConnCreationDesc = engineConnCreationDesc

  override def getServiceInstance: ServiceInstance = instance

  override def setServiceInstance(instance: ServiceInstance): Unit = this.instance = instance

  override def getPid: String = pid

  override def setPid(pid: String): Unit = this.pid = pid

  override def getEngineConnManagerEnv: EngineConnManagerEnv = if ( null == this.ecmEnv ){
    getEngineConnLaunchRunner.getEngineConnLaunch.getEngineConnManagerEnv()
  } else {
    this.ecmEnv
  }

  override def setEngineConnManagerEnv(env: EngineConnManagerEnv): Unit = this.ecmEnv = env

  override def toString = s"DefaultEngineConn($status, $tickedId, $resource, $labels, $engineConnCreationDesc, $engineConnInfo, $ecmEnv, $engineConnLaunchRunner, $instance, $pid)"
}
