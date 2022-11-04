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

package org.apache.linkis.engineconn.executor.entity

import org.apache.linkis.manager.common.entity.enumeration.NodeStatus

trait SensibleExecutor extends Executor {

  protected var status: NodeStatus = NodeStatus.Starting

  private var lastActivityTime = System.currentTimeMillis

  def getLastActivityTime: Long = lastActivityTime

  def updateLastActivityTime(): Unit = lastActivityTime = System.currentTimeMillis

  def getStatus: NodeStatus = status

  protected def onStatusChanged(fromStatus: NodeStatus, toStatus: NodeStatus): Unit

  def transition(toStatus: NodeStatus): Unit = {
    lastActivityTime = System.currentTimeMillis
    this.status match {
      case NodeStatus.Failed | NodeStatus.Success =>
        logger.warn(s"$toString attempt to change status ${this.status} => $toStatus, ignore it.")
        return
      case NodeStatus.ShuttingDown =>
        toStatus match {
          case NodeStatus.Failed | NodeStatus.Success =>
          case _ =>
            logger.warn(
              s"$toString attempt to change a Executor from ShuttingDown to $toStatus, ignore it."
            )
            return
        }
      case _ =>

    }
    if (!NodeStatus.isCompleted(toStatus)) {
      logger.info(s"Waitiing lock release, to change status $status=>$toStatus.")
      this synchronized {
        logger.info(s"Finished wait lock release, to change status $status=>$toStatus.")
      }
    }
    logger.info(s"$toString changed status $status => $toStatus.")
    val oldState = status
    this.status = toStatus
    onStatusChanged(oldState, toStatus)
  }

}

object SensibleExecutor {

  lazy val defaultErrorSensibleExecutor: SensibleExecutor = new SensibleExecutor {
    override def getStatus: NodeStatus = NodeStatus.ShuttingDown

    override protected def onStatusChanged(fromStatus: NodeStatus, toStatus: NodeStatus): Unit = {}

    override def getId: String = "0"

    override def init(): Unit = {}

    override def tryReady(): Boolean = false

    override def tryShutdown(): Boolean = true

    override def tryFailed(): Boolean = true

    override def isClosed(): Boolean = true

    override def trySucceed(): Boolean = false
  }

  def getDefaultErrorSensibleExecutor: SensibleExecutor = defaultErrorSensibleExecutor

}
