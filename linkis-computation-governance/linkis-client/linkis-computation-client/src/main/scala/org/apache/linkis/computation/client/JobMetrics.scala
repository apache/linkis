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

package org.apache.linkis.computation.client

import org.apache.linkis.common.utils.{ByteTimeUtils, Logging}

import java.util

import scala.collection.JavaConverters._

trait ClientMetrics {

  def getMetrics: Map[String, AnyRef]
  def getMetricString: String
  def printIt(): Unit

}

abstract class AbstractJobMetrics extends ClientMetrics with Logging {
  override def printIt(): Unit = logger.info(getMetricString)
}

class LinkisJobMetrics(taskId: String) extends AbstractJobMetrics {

  private var clientSubmitTime: Long = 0
  private var clientFinishedTime: Long = 0
  private var clientGetJobInfoTime: Long = 0
  private var clientFetchResultSetTime: Long = 0
  private val metricsMap = new util.HashMap[String, AnyRef]

  def setClientSubmitTime(clientSubmitTime: Long): Unit = this.clientSubmitTime = clientSubmitTime

  def setClientFinishedTime(clientFinishedTime: Long): Unit = this.clientFinishedTime =
    clientFinishedTime

  def addClientGetJobInfoTime(getJobInfoTime: Long): Unit =
    this.clientGetJobInfoTime += getJobInfoTime

  def addClientFetchResultSetTime(fetchResultSetTime: Long): Unit =
    this.clientFetchResultSetTime = clientFetchResultSetTime

  def setLong(key: String, value: Long): Unit = metricsMap.put(key, value: java.lang.Long)

  def addLong(key: String, value: Long): Unit = {
    val v = if (metricsMap.containsKey(key)) metricsMap.get(key).asInstanceOf[Long] else 0
    setLong(key, value + v: java.lang.Long)
  }

  override def getMetrics: Map[String, AnyRef] = {
    metricsMap.put("clientSubmitTime", clientSubmitTime: java.lang.Long)
    metricsMap.put("clientFinishedTime", clientFinishedTime: java.lang.Long)
    metricsMap.put("clientGetJobInfoTime", clientGetJobInfoTime: java.lang.Long)
    metricsMap.put("clientFetchResultSetTime", clientFetchResultSetTime: java.lang.Long)
    metricsMap.asScala.toMap
  }

  override def getMetricString: String =
    s"The metrics of job($taskId), costs ${ByteTimeUtils.msDurationToString(clientFinishedTime - clientSubmitTime)} to execute, costs ${ByteTimeUtils
      .msDurationToString(clientFetchResultSetTime)} to fetch all resultSets."

}
