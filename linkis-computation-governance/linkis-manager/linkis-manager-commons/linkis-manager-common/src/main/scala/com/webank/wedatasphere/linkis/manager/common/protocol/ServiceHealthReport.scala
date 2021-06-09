package com.webank.wedatasphere.linkis.manager.common.protocol


abstract class ServiceHealthReport {

  def getReportTime:Long
  def setReportTime(reportTime:Long):Unit

  def setServiceState

}
