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

package org.apache.linkis.engineconn.computation.executor.upstream.wrapper

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import org.apache.linkis.engineconn.computation.executor.entity.EngineConnTask
import org.apache.linkis.engineconn.computation.executor.execute.ComputationExecutor
import org.apache.linkis.engineconn.computation.executor.upstream.entity.{
  ECTaskEntranceConnection,
  UpstreamConnection
}

import org.apache.commons.lang3.StringUtils

class ECTaskEntranceConnectionWrapper(
    taskID: String,
    engineConnTask: EngineConnTask,
    executor: ComputationExecutor
) extends ConnectionInfoWrapper
    with Logging {

  /* delete if time for any entry being in map exceeds threshold */
  private val wrapperEntriesSurviveThresholdSec =
    ComputationExecutorConf.UPSTREAM_MONITOR_WRAPPER_ENTRIES_SURVIVE_THRESHOLD_SEC

  @volatile
  private var connectionInfo: ECTaskEntranceConnection = _

  @volatile
  private var lastUpdateTime: Long = System.currentTimeMillis()

  override def getKey(): String = taskID

  def getEngineConnTask(): EngineConnTask = engineConnTask

  def getExecutor(): ComputationExecutor = executor

  override def updateConnectionInfo(newInfo: UpstreamConnection): Unit = newInfo match {
    case newInfo2: ECTaskEntranceConnection =>
      if (connectionInfo == null || StringUtils.isBlank(connectionInfo.getKey)) {
        connectionInfo = newInfo2
      } else if (!connectionInfo.isSameConnectionAs(newInfo2)) {
        logger.error(
          "Failed to update connection-info: target connection-info is not same as current." +
            "current: " + connectionInfo.getKey + ", " + connectionInfo.getUpstreamServiceInstanceName +
            "target: " + newInfo2.getKey + ", " + newInfo2.getUpstreamServiceInstanceName
        )
      } else if (newInfo2.getPrevUpdatedAliveTimestamp() != -1L) {
        connectionInfo = newInfo2
      }
      lastUpdateTime = System.currentTimeMillis
    case _ =>
      logger.error("wrong data-type for UpstreamConnection:" + newInfo.getClass.getCanonicalName)
  }

  override def getUpstreamConnection(): ECTaskEntranceConnection = connectionInfo

  override def getLastUpdateTime(): Long = lastUpdateTime // createTime

  override def shouldClear(): Boolean = {
    System.currentTimeMillis - lastUpdateTime >= wrapperEntriesSurviveThresholdSec * 1000
  }

}
