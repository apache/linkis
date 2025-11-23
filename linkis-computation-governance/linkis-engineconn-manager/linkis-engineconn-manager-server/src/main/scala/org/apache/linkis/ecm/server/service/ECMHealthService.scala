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

package org.apache.linkis.ecm.server.service

import org.apache.linkis.ecm.core.report.ECMHealthReport
import org.apache.linkis.manager.common.entity.enumeration.{NodeHealthy, NodeStatus}
import org.apache.linkis.manager.common.protocol.node.{
  NodeHealthyRequest,
  NodeHeartbeatMsg,
  NodeHeartbeatRequest
}

trait ECMHealthService {

  def getLastEMHealthReport: ECMHealthReport

  def reportHealth(report: ECMHealthReport): Unit

  def generateHealthReport(reportTime: Long): ECMHealthReport

  def dealNodeHeartbeatRequest(nodeHeartbeatRequest: NodeHeartbeatRequest): NodeHeartbeatMsg

  def dealNodeHealthyRequest(nodeHealthyRequest: NodeHealthyRequest): Unit

  def getNodeStatus: NodeStatus

  def getNodeHealthy: NodeHealthy

  def isSetByManager: Boolean

  def transitionStatus(toStatus: NodeStatus): Unit

  def transitionHealthy(toHealthy: NodeHealthy): Unit

}
