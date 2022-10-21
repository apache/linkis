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

package org.apache.linkis.httpclient.discovery

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.httpclient.Client
import org.apache.linkis.httpclient.config.HttpClientConstant
import org.apache.linkis.httpclient.errorcode.LinkisHttpclientErrorCodeSummary.CONNECT_TO_SERVERURL
import org.apache.linkis.httpclient.exception.DiscoveryException

import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpResponse

import java.io.Closeable
import java.net.ConnectException
import java.text.MessageFormat
import java.util
import java.util.concurrent.ScheduledFuture

import scala.collection.JavaConverters._
import scala.concurrent.duration.TimeUnit

abstract class AbstractDiscovery extends Discovery with Closeable with Logging {

  private var serverUrl: String = _
  private var period: Long = _
  private var timeUnit: TimeUnit = _
  private var client: Client = _
  private val discoveryListeners = new util.ArrayList[DiscoveryListener]
  private val serverInstances = new util.HashSet[String]
  private val unhealthyServerInstances = new util.HashSet[String]

  private var discoveryFuture: ScheduledFuture[_] = _
  private var heartbeatFuture: ScheduledFuture[_] = _
  private var unhealthyHeartbeatFuture: ScheduledFuture[_] = _

  override def setServerUrl(serverUrl: String): Unit = {
    this.serverUrl = serverUrl
  }

  def getServerUrl: String = serverUrl

  def setSchedule(period: Long, timeUnit: TimeUnit): Unit = {
    this.period = period
    this.timeUnit = timeUnit
  }

  def setClient(client: Client): Unit = this.client = client

  def getClient: Client = client

  override def start(): Unit = {
    val delayTime = if (period < 10) 1 else period / 5
    discoveryFuture = startDiscovery()
    heartbeatFuture = startHealthyCheck(delayTime)
    unhealthyHeartbeatFuture = startUnHealthyCheck(delayTime)
  }

  def startHealthyCheck(delayTime: Long): ScheduledFuture[_] = {
    logger.info("start HealthyCheck thread")
    Utils.defaultScheduler.scheduleAtFixedRate(
      new Runnable {
        override def run(): Unit = {
          serverInstances.asScala.foreach { serverUrl =>
            val action = getHeartbeatAction(serverUrl)
            logger.info("heartbeat to healthy gateway " + serverUrl)
            Utils.tryCatch(client.execute(action, 3000) match {
              case heartbeat: HeartbeatResult =>
                if (!heartbeat.isHealthy) {
                  addUnhealthyServerInstances(serverUrl)
                }
            }) { case _: ConnectException =>
              addUnhealthyServerInstances(serverUrl)
            }
          }
        }
      },
      delayTime,
      period,
      timeUnit
    )
  }

  def startDiscovery(): ScheduledFuture[_] = {
    logger.info("start Discovery thread")
    client.execute(getHeartbeatAction(serverUrl), 3000) match {
      case heartbeat: HeartbeatResult =>
        if (!heartbeat.isHealthy) {
          throw new DiscoveryException(
            MessageFormat.format(CONNECT_TO_SERVERURL.getErrorDesc, serverUrl)
          )
        } else discoveryListeners.asScala.foreach(_.onServerDiscovered(serverUrl))
    }

    Utils.defaultScheduler.scheduleAtFixedRate(
      new Runnable {
        override def run(): Unit = Utils.tryAndWarn {
          logger.info("to discovery gateway" + serverUrl)
          val serverUrls = discovery()
          addServerInstances(serverUrls)
        }
      },
      1,
      period + 30,
      timeUnit
    )
  }

  def startUnHealthyCheck(delayTime: Long): ScheduledFuture[_] = {
    logger.info("start UnHealthyCheck thread")
    Utils.defaultScheduler.scheduleAtFixedRate(
      new Runnable {
        override def run(): Unit = {
          unhealthyServerInstances.asScala.foreach { serverUrl =>
            val action = getHeartbeatAction(serverUrl)
            logger.info("heartbeat to unhealthy gateway " + serverUrl)
            Utils.tryCatch(client.execute(action, 3000) match {
              case heartbeat: HeartbeatResult =>
                if (heartbeat.isHealthy) {
                  unhealthyServerInstances synchronized unhealthyServerInstances.remove(serverUrl)
                  discoveryListeners.asScala.foreach(_.onServerHealthy(serverUrl))
                  serverInstances synchronized serverInstances.add(serverUrl)
                } else if (serverInstances.contains(serverUrl)) {
                  serverInstances synchronized serverInstances.remove(serverUrl)
                }
            }) { case _: ConnectException =>
              unhealthyServerInstances synchronized unhealthyServerInstances.remove(serverUrl)
              serverInstances synchronized serverInstances.remove(serverUrl)
              discoveryListeners.asScala.foreach(_.onServerUnconnected(serverUrl))
            }
          }
        }
      },
      delayTime,
      period,
      timeUnit
    )
  }

  protected def discovery(): Array[String]

  protected def getHeartbeatAction(serverUrl: String): HeartbeatAction

  def getHeartbeatResult(response: HttpResponse, requestAction: HeartbeatAction): HeartbeatResult

  override def addDiscoveryListener(discoveryListener: DiscoveryListener): Unit =
    discoveryListeners.add(discoveryListener)

  override def getServerInstances: Array[String] =
    serverInstances.toArray(new Array[String](serverInstances.size()))

  override def close(): Unit = {
    if (null != discoveryFuture) discoveryFuture.cancel(true)
    if (null != heartbeatFuture) heartbeatFuture.cancel(true)
    if (null != unhealthyHeartbeatFuture) unhealthyHeartbeatFuture.cancel(true)
  }

  def addUnhealthyServerInstances(unhealthyUrl: String): Unit = {
    logger.info(s"add  ${unhealthyUrl} to unhealthy list ")
    val updateUnhealthyUrl = if (serverInstances.contains(unhealthyUrl)) {
      unhealthyUrl
    } else if (serverInstances.contains(unhealthyUrl + HttpClientConstant.PATH_SPLIT_TOKEN)) {
      unhealthyUrl + HttpClientConstant.PATH_SPLIT_TOKEN
    } else {
      ""
    }
    if (StringUtils.isBlank(updateUnhealthyUrl)) {
      logger.info(s"${unhealthyUrl}  unhealthy url not exists")
    } else {
      unhealthyServerInstances synchronized unhealthyServerInstances.add(updateUnhealthyUrl)
      discoveryListeners.asScala.foreach(_.onServerUnhealthy(updateUnhealthyUrl))
      serverInstances synchronized serverInstances.remove(updateUnhealthyUrl)
    }
  }

  def addServerInstances(serverUrls: Array[String]): Unit = {
    serverInstances synchronized serverUrls.foreach { url =>
      if (!serverInstances.contains(url)) {
        val parsedUrl =
          if (url.endsWith(HttpClientConstant.PATH_SPLIT_TOKEN)) url.substring(0, url.length - 1)
          else url
        serverInstances.add(parsedUrl)
        discoveryListeners.asScala.foreach(_.onServerDiscovered(parsedUrl))
      }
    }
  }

}
