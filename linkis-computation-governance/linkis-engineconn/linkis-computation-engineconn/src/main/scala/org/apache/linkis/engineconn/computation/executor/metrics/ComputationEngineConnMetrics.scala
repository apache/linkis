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

package org.apache.linkis.engineconn.computation.executor.metrics

import org.apache.linkis.manager.common.entity.enumeration.NodeStatus

import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}

object ComputationEngineConnMetrics {

  private var lastUpdateTimeMills = System.currentTimeMillis()

  private var lastUnlockTimeMills = System.currentTimeMillis()

  private val totalUnlockTimeDurationMills = new AtomicLong()

  private val totalIdleTimeDurationMills = new AtomicLong()

  private val totalBusyTimeDurationMills = new AtomicLong()

  private val isKilled = new AtomicBoolean(false)

  private val unlockToShutdownDurationMills = new AtomicLong()

  def updateMetrics(oldStatus: NodeStatus, newStatus: NodeStatus): Unit = {
    if (oldStatus != newStatus) {
      val oldUpdateTimeMills = lastUpdateTimeMills
      lastUpdateTimeMills = System.currentTimeMillis()
      val durationMills = lastUpdateTimeMills - oldUpdateTimeMills
      oldStatus match {
        case NodeStatus.Unlock =>
          totalUnlockTimeDurationMills.addAndGet(durationMills)
        case NodeStatus.Idle =>
          totalIdleTimeDurationMills.addAndGet(durationMills)
        case NodeStatus.Busy =>
          totalBusyTimeDurationMills.addAndGet(durationMills)
        case _ =>
      }
      if (NodeStatus.Unlock == newStatus) {
        lastUnlockTimeMills = System.currentTimeMillis()
      }
      if (NodeStatus.ShuttingDown == newStatus && NodeStatus.Unlock == oldStatus) {
        unlockToShutdownDurationMills.set(System.currentTimeMillis() - lastUnlockTimeMills)
      }
    }
  }

  def getTotalUnLockTimeMills(nodeStatus: NodeStatus): Long = {
    nodeStatus match {
      case NodeStatus.Unlock =>
        totalUnlockTimeDurationMills.get() + System.currentTimeMillis() - lastUpdateTimeMills
      case _ =>
        totalUnlockTimeDurationMills.get()
    }
  }

  def getTotalIdleTimeMills(nodeStatus: NodeStatus): Long = {
    nodeStatus match {
      case NodeStatus.Idle =>
        totalIdleTimeDurationMills.get() + System.currentTimeMillis() - lastUpdateTimeMills
      case _ =>
        totalIdleTimeDurationMills.get()
    }
  }

  def getTotalBusyTimeMills(nodeStatus: NodeStatus): Long = {
    nodeStatus match {
      case NodeStatus.Busy =>
        totalBusyTimeDurationMills.get() + System.currentTimeMillis() - lastUpdateTimeMills
      case _ =>
        totalBusyTimeDurationMills.get()
    }
  }

  def getTotalLockTimeMills(nodeStatus: NodeStatus): Long =
    getTotalBusyTimeMills(nodeStatus) + getTotalIdleTimeMills(nodeStatus)

  def getUnlockToShutdownDurationMills(): Long = unlockToShutdownDurationMills.get()

  def getLastUnlockTimestamp(nodeStatus: NodeStatus): Long = {
    nodeStatus match {
      case NodeStatus.Unlock => lastUnlockTimeMills
      case _ => 0
    }
  }

}
