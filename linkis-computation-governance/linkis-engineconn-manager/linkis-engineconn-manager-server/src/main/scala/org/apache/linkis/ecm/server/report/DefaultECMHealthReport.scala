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

package org.apache.linkis.ecm.server.report

import org.apache.linkis.ecm.core.engineconn.EngineConn
import org.apache.linkis.ecm.core.report.ECMHealthReport
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.metrics.NodeOverLoadInfo
import org.apache.linkis.manager.common.entity.resource.Resource

class DefaultECMHealthReport extends ECMHealthReport {

  private var protectedResource: Resource = _

  private var totalResource: Resource = _

  private var usedResource: Resource = _

  private var overload: NodeOverLoadInfo = _

  private var runningEngineConns: Array[EngineConn] = _

  private var reportTime: Long = _

  private var nodeStatus: NodeStatus = _

  private var nodeMsg: String = _

  private var nodeId: String = _

  override def setProtectedResource(protectedResource: Resource): Unit = this.protectedResource =
    protectedResource

  override def getProtectResource: Resource = protectedResource

  override def setOverload(overload: NodeOverLoadInfo): Unit = this.overload = overload

  override def getOverload: NodeOverLoadInfo = overload

  override def setRunningEngineConns(runningEngines: Array[EngineConn]): Unit =
    this.runningEngineConns = runningEngines

  override def getRunningEngineConns: Array[EngineConn] = runningEngineConns

  override def getReportTime: Long = reportTime

  override def setReportTime(reportTime: Long): Unit = this.reportTime = reportTime

  override def getNodeStatus: NodeStatus = nodeStatus

  override def setNodeStatus(nodeStatus: NodeStatus): Unit = this.nodeStatus = nodeStatus

  override def setNodeMsg(nodeMsg: String): Unit = this.nodeMsg = nodeMsg

  override def getNodeMsg: String = nodeMsg

//  override def getUsedResource: Resource = usedResource

//  override def setUsedResource(usedResource: Resource): Unit = this.usedResource = usedResource

  override def getTotalResource: Resource = totalResource

  override def setTotalResource(totalResource: Resource): Unit = this.totalResource = totalResource

  override def setNodeId(nodeId: String): Unit = this.nodeId = nodeId

  override def getNodeId: String = nodeId
}
