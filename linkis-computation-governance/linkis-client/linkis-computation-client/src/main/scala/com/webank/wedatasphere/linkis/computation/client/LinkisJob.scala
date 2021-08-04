package com.webank.wedatasphere.linkis.computation.client

import com.webank.wedatasphere.linkis.computation.client.operator.Operator

/**
  * Created by enjoyyin on 2021/6/1.
  */
trait LinkisJob {

  def getId: String

  def kill(): Unit

  def getOperator(operatorName: String): Operator[_]

  def isCompleted: Boolean

  def isSucceed: Boolean

  def waitForCompleted(): Unit

  def waitFor(mills: Long): Unit

  def getJobMetrics: LinkisJobMetrics

}