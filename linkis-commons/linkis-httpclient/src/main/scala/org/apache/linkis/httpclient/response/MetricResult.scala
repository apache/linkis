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
 
package org.apache.linkis.httpclient.response

import java.util


trait MetricResult {

  def getMetric: HttpMetric

  def setMetric(metric: HttpMetric): Unit

}

trait AbstractMetricResult extends MetricResult {

  private var metric: HttpMetric = _

  override def getMetric: HttpMetric = metric

  override def setMetric(metric: HttpMetric): Unit = this.metric = metric
}

class HttpMetric {

  private var prepareReqTime: Long = 0
  private var executeTotalTime: Long = 0
  private var deserializeTime: Long = 0
  private val attempts = new util.ArrayList[Long]

  def setPrepareReqTime(prepareReqTime: Long): Unit = this.prepareReqTime = prepareReqTime
  def getPrepareReqTime: Long = prepareReqTime
  def setExecuteTotalTime(executeTotalTime: Long): Unit = this.executeTotalTime = executeTotalTime
  def getExecuteTotalTime: Long = executeTotalTime
  def setDeserializeTime(deserializeTime: Long): Unit = this.deserializeTime = deserializeTime
  def getDeserializeTime: Long = deserializeTime

  def addRetry(attemptTime: Long): Unit = attempts.add(attemptTime)
  def addRetries(attempts: java.util.List[Long]): Unit = this.attempts.addAll(attempts)
  def getAttemptTimes: util.List[Long] = attempts

  def getMetricMap: Map[String, Any] = Map("prepareReqTime" -> prepareReqTime, "executeTotalTime" -> executeTotalTime, "deserializeTime" -> deserializeTime,
    "retriedNum" -> attempts.size, "attempts" -> attempts)

}