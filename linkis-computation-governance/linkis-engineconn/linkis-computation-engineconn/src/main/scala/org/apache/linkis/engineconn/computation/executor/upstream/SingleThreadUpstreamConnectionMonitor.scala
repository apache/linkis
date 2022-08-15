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

package org.apache.linkis.engineconn.computation.executor.upstream

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.common.exception.EngineConnException
import org.apache.linkis.engineconn.computation.executor.upstream.access.{
  ConnectionInfoAccess,
  ConnectionInfoAccessRequest
}
import org.apache.linkis.engineconn.computation.executor.upstream.handler.{
  MonitorHandler,
  MonitorHandlerRequest
}
import org.apache.linkis.engineconn.computation.executor.upstream.wrapper.ConnectionInfoWrapper
import org.apache.linkis.engineconn.computation.executor.utlis.ComputationErrorCode

import org.apache.commons.lang3.concurrent.BasicThreadFactory

import java.util
import java.util.Collections
import java.util.concurrent.{ConcurrentHashMap, ScheduledThreadPoolExecutor, TimeUnit}

abstract class SingleThreadUpstreamConnectionMonitor(
    name: String,
    infoAccess: ConnectionInfoAccess,
    handler: MonitorHandler
) extends UpstreamConnectionMonitor
    with Logging {

  protected val wrapperMap = new ConcurrentHashMap[String, ConnectionInfoWrapper]

  private val monitorDaemon = new ScheduledThreadPoolExecutor(
    3,
    new BasicThreadFactory.Builder().namingPattern(name + "-%d").daemon(true).build
  )

  private var started = false

  override def getUpstreamNodeInfoAccess(): ConnectionInfoAccess = infoAccess

  override def getHandler(): MonitorHandler = handler

  def generateInfoAccessRequest(
      wrapperList: util.List[ConnectionInfoWrapper]
  ): ConnectionInfoAccessRequest

  def generateHandlerRequest(wrapperList: util.List[ConnectionInfoWrapper]): MonitorHandlerRequest

  def start(): Unit = this.synchronized {
    if (!started) {
      panicIfNull(infoAccess, "infoAccess should not be null")
      panicIfNull(handler, "handler should not be null")
      logger.info("started upstream monitor")
      monitorDaemon.scheduleAtFixedRate(
        new Runnable {
          override def run(): Unit = Utils.tryCatch(scanOneIteration) { t =>
            logger.error("ClientHeartbeatMonitor failed to scan for one iteration", t)
          }
        },
        0,
        5,
        TimeUnit.SECONDS
      )
      monitorDaemon.scheduleAtFixedRate(
        new Runnable {
          override def run(): Unit = Utils.tryCatch(clearOneIteration) { t =>
            logger.error("clearWrapperMap has failed for one iteration", t)
          }
        },
        0,
        1,
        TimeUnit.HOURS
      )
      Utils.addShutdownHook(() -> this.shutdown())
    } else {
      throw new EngineConnException(
        ComputationErrorCode.START_UPSTREAM_MONITOR_TWICE,
        "cannot start upstream-monitor twice!"
      )
    }
    started = true
  }

  def scanOneIteration(): Unit = {
    panicIfNull(infoAccess, "connectionInfoAccess should not be null")
    panicIfNull(handler, "handler should not be null")

    val toBeRequested = new util.ArrayList[ConnectionInfoWrapper]
    toBeRequested.addAll(wrapperMap.values())

    if (toBeRequested.size() == 0) {
      logger.debug("nothing to monitor")
      return
    }
    logger.info(
      "requesting connection info: " + util.Arrays
        .toString(Collections.list(wrapperMap.keys).toArray())
    )
    val infoAccessRequest = generateInfoAccessRequest(toBeRequested)
    val connectionInfoList = infoAccess.getUpstreamInfo(infoAccessRequest)
    logger.info(
      "connection-info result: " + connectionInfoList(0)
        .getUpstreamServiceInstanceName() + " : " + connectionInfoList(0).isAlive()
    )
    if (connectionInfoList == null || connectionInfoList.size == 0) {
      logger.info("Found none upstream-info")
      return
    }

    val toBeHandled: util.List[ConnectionInfoWrapper] = new util.ArrayList[ConnectionInfoWrapper]
    val connectionInfoMap = connectionInfoList.map(x => (x.getKey(), x)).toMap
    val entries = wrapperMap.entrySet.iterator()
    while (entries.hasNext) {
      val entry = entries.next
      val key = entry.getKey
      val value = entry.getValue
      if (connectionInfoMap.contains(key)) {
        value.updateConnectionInfo(connectionInfoMap.get(key).get)
      }
      if (!value.getUpstreamConnection().isAlive()) {
        logger.info("Found upstream connection problem: " + entry.getValue.toString)
        toBeHandled.add(value)
      }
    }

    val iterator = toBeHandled.iterator
    while (iterator.hasNext) {
      wrapperMap.remove(iterator.next.getKey)
    }

    if (toBeHandled.size > 0) {
      val handlerRequest = generateHandlerRequest(toBeHandled)
      Utils.tryCatch(handler.handle(handlerRequest)) { t =>
        logger.error("failed to handle upstream connection-loss", t)
      }
    }
  }

  private def clearOneIteration(): Unit = {
    val entries = wrapperMap.entrySet.iterator
    while (entries.hasNext) {
      val entry = entries.next
      if (entry.getValue.shouldClear) {
        entries.remove()
      }
    }
  }

  protected def panicIfNull(obj: Any, msg: String): Unit = {
    if (obj == null) {
      throw new EngineConnException(ComputationErrorCode.VARIABLE_NULL_ERROR_CODE, msg)
    }
  }

  def shutdown(): Unit = this.synchronized {
    if (started) {
      logger.info("stopping upstream monitor")
      monitorDaemon.shutdownNow
    }
  }

}
