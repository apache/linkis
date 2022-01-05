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
 
package org.apache.linkis.ecm.server.metrics


import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import org.apache.linkis.ecm.core.engineconn.EngineConn
import org.apache.linkis.ecm.core.metrics.ECMMetrics
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus._

import scala.collection.JavaConversions._


class DefaultECMMetrics extends ECMMetrics {


  private val startingEngineConnCount = new AtomicInteger(0)

  private val runningEngineConnCount = new AtomicInteger(0)

  private val successEngineConnCount = new AtomicInteger(0)

  private val failedEngineConnCount = new AtomicInteger(0)

  private val startingEngineConnMap = new ConcurrentHashMap[String, EngineConn](64)

  private val runningEngineConnMap = new ConcurrentHashMap[String, EngineConn](64)

  private val successEngineConnMap = new ConcurrentHashMap[String, EngineConn](64)

  private val failedEngineConnMap = new ConcurrentHashMap[String, EngineConn](64)

  override def getStartingEngineConns: Array[EngineConn] = getEngineConns(startingEngineConnMap)

  override def getRunningEngineConns: Array[EngineConn] = getEngineConns(runningEngineConnMap)

  override def getSucceedEngineConns: Array[EngineConn] = getEngineConns(successEngineConnMap)

  override def getFailedEngineConns: Array[EngineConn] = getEngineConns(failedEngineConnMap)

  private val getEngineConns = (map: ConcurrentHashMap[String, EngineConn]) => map.values().toSeq.toArray

  private val decreaseEngineConnMetric = (engineConn: EngineConn, map: ConcurrentHashMap[String, EngineConn], count: AtomicInteger) => {
    val conn = map.remove(engineConn.getTickedId)
    if (conn != null) {
      count.decrementAndGet()
    }
  }

  private val increaseEngineConnMetric = (engineConn: EngineConn, map: ConcurrentHashMap[String, EngineConn], count: AtomicInteger, status: NodeStatus) => {
    if (engineConn.getStatus.equals(status)) {
      count.incrementAndGet()
      map.put(engineConn.getTickedId, engineConn)
    }
  }

  override def increaseStartingEngineConn(engineConn: EngineConn): Unit = {
    increaseEngineConnMetric(engineConn, startingEngineConnMap, startingEngineConnCount, Starting)
  }

  def decreseStartingEngineConn(engineConn: EngineConn): Unit = {
    decreaseEngineConnMetric(engineConn, startingEngineConnMap, startingEngineConnCount)
  }

  override def increaseRunningEngineConn(engineConn: EngineConn): Unit = {
    increaseEngineConnMetric(engineConn, runningEngineConnMap, runningEngineConnCount, Running)
  }

  def decreseRunningEngineConn(engineConn: EngineConn): Unit = {
    decreaseEngineConnMetric(engineConn, runningEngineConnMap, runningEngineConnCount)
  }

  override def increaseSuccessEngineConn(engineConn: EngineConn): Unit = {
    increaseEngineConnMetric(engineConn, successEngineConnMap, successEngineConnCount, Success)
  }

  def decreaseSuccessEngineConn(engineConn: EngineConn): Unit = {
    decreaseEngineConnMetric(engineConn, successEngineConnMap, successEngineConnCount)
  }

  override def increaseFailedEngineConn(engineConn: EngineConn): Unit = {
    increaseEngineConnMetric(engineConn, failedEngineConnMap, failedEngineConnCount, Failed)
  }

  def decreaseFailedEngineConn(engineConn: EngineConn): Unit = {
    decreaseEngineConnMetric(engineConn, failedEngineConnMap, failedEngineConnCount)
  }

}
