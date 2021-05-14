package com.webank.wedatasphere.linkis.httpclient.response

import java.util

/**
 * Created by enjoyyin on 2020/10/28.
 */
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