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

package org.apache.linkis.engineconn.computation.executor.upstream.entity

import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf

import org.apache.commons.lang3.StringUtils

class ECTaskEntranceConnection(
    taskID: String,
    currentServiceInstanceName: String,
    upstreamServiceInstanceName: String
) extends UpstreamConnection {

  @volatile
  private var prevUpdatedAliveTimestamp = -1L

  override def updatePrevAliveTimeStamp(target: Long): Unit = {
    prevUpdatedAliveTimestamp = target
  }

  override def getPrevUpdatedAliveTimestamp(): Long = prevUpdatedAliveTimestamp

  override def isAlive(): Boolean = {
    System
      .currentTimeMillis() - prevUpdatedAliveTimestamp <= ComputationExecutorConf.UPSTREAM_MONITOR_ECTASK_ENTRANCE_THRESHOLD_SEC * 1000
  }

  override def isSameConnectionAs(upstreamConnection: UpstreamConnection): Boolean =
    upstreamConnection match {
      case upstreamConnection2: ECTaskEntranceConnection =>
        StringUtils.equals(upstreamConnection2.getKey, this.getKey) &&
          StringUtils.equals(upstreamConnection2.getTaskID, this.getTaskID) &&
          StringUtils.equals(
            upstreamConnection2.getCurrentServiceInstanceName,
            this.getCurrentServiceInstanceName
          ) &&
          StringUtils.equals(
            upstreamConnection2.getUpstreamServiceInstanceName,
            this.getUpstreamServiceInstanceName
          )
      case _ => false
    }

  override def getKey(): String = taskID

  def getTaskID(): String = taskID

  override def getCurrentServiceInstanceName(): String = currentServiceInstanceName

  override def getUpstreamServiceInstanceName(): String = upstreamServiceInstanceName
}
