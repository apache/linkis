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
 
package org.apache.linkis.httpclient.discovery

import java.io.Closeable
import java.net.ConnectException
import java.util
import java.util.concurrent.ScheduledFuture

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.httpclient.Client
import org.apache.linkis.httpclient.exception.DiscoveryException
import org.apache.http.HttpResponse

import scala.collection.JavaConversions._
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
    client.execute(getHeartbeatAction(serverUrl), 3000) match {
      case heartbeat: HeartbeatResult => if(!heartbeat.isHealthy)
        throw new DiscoveryException(s"connect to serverUrl $serverUrl failed! Reason: gateway server is unhealthy!")
      else discoveryListeners.foreach(_.onServerDiscovered(serverUrl))
    }
    val delayTime = timeUnit.convert(timeUnit.toMillis(period)/5, timeUnit)
    discoveryFuture = Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = Utils.tryAndWarn {
        val serverUrls = discovery()
        serverInstances synchronized serverUrls.foreach { serverUrl =>
          if(!serverInstances.contains(serverUrl)) {
            serverInstances.add(serverUrl)
            discoveryListeners.foreach(_.onServerDiscovered(serverUrl))
          }
        }
      }
    }, delayTime, period, timeUnit)
    heartbeatFuture = Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        serverInstances.toList.foreach { serverUrl =>
          val action = getHeartbeatAction(serverUrl)
          info("heartbeat to healthy gateway " + serverUrl)
          Utils.tryCatch(client.execute(action, 3000) match {
            case heartbeat: HeartbeatResult =>
              if(!heartbeat.isHealthy) {
                unhealthyServerInstances synchronized unhealthyServerInstances.add(serverUrl)
                discoveryListeners.foreach(_.onServerUnhealthy(serverUrl))
                serverInstances synchronized serverInstances.remove(serverUrl)
              }
          }) {
            case _: ConnectException =>
              unhealthyServerInstances synchronized unhealthyServerInstances.remove(serverUrl)
              serverInstances synchronized serverInstances.remove(serverUrl)
              discoveryListeners.foreach(_.onServerUnconnected(serverUrl))
          }
        }
      }
    }, delayTime, period, timeUnit)
    unhealthyHeartbeatFuture = Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        unhealthyServerInstances.toList.foreach { serverUrl =>
          val action = getHeartbeatAction(serverUrl)
          info("heartbeat to unhealthy gateway " + serverUrl)
          Utils.tryCatch(client.execute(action, 3000) match {
            case heartbeat: HeartbeatResult =>
              if(heartbeat.isHealthy) {
                unhealthyServerInstances synchronized unhealthyServerInstances.remove(serverUrl)
                discoveryListeners.foreach(_.onServerHealthy(serverUrl))
                serverInstances synchronized serverInstances.add(serverUrl)
              } else if(serverInstances.contains(serverUrl)) serverInstances synchronized serverInstances.remove(serverUrl)
          }) {
            case _: ConnectException =>
              unhealthyServerInstances synchronized unhealthyServerInstances.remove(serverUrl)
              serverInstances synchronized serverInstances.remove(serverUrl)
              discoveryListeners.foreach(_.onServerUnconnected(serverUrl))
          }
        }
      }
    }, delayTime, period, timeUnit)
  }

  protected def discovery(): Array[String]

  protected def getHeartbeatAction(serverUrl: String): HeartbeatAction

  def getHeartbeatResult(response: HttpResponse, requestAction: HeartbeatAction): HeartbeatResult

  override def addDiscoveryListener(discoveryListener: DiscoveryListener): Unit =
    discoveryListeners.add(discoveryListener)

  override def getServerInstances: Array[String] = serverInstances.toArray(new Array[String](serverInstances.size()))

  override def close(): Unit = {
    discoveryFuture.cancel(true)
    heartbeatFuture.cancel(true)
    unhealthyHeartbeatFuture.cancel(true)
  }
}
