package com.webank.wedatasphere.linkis.manager.common.protocol

/**
 * created by cooperyang on 2020/7/24
 * Description:
 */
abstract class ServiceHealthReport {

  def getReportTime:Long
  def setReportTime(reportTime:Long):Unit

  def setServiceState

}
