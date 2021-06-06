package com.webank.wedatasphere.linkis.ujes.client.request

import com.webank.wedatasphere.linkis.httpclient.request.GetAction
import org.apache.commons.lang.StringUtils

import scala.collection.mutable.ArrayBuffer


class JobListAction extends GetAction with UJESJobAction {

  override def suffixURLs: Array[String] = Array("jobhistory", "list")

}

/*
startDate
endDate
status
pageNow
pageSize
taskID
executeApplicationName
proxyUser
 */

object JobListAction {
  def newBuilder(): Builder = new Builder

  class Builder private[JobListAction]() {
    private var proxyUser: String = _
    private var startDateMills: Long = 0
    private var endDateMills: Long = 0
    private var statusList: ArrayBuffer[String] = new ArrayBuffer[String]()
    private var pageNow: Int = 0
    private var pageSize: Int = 0
    private var taskID: Long = _
    private var executeApplicationName: String = _

    def setProxyUser(user: String): Builder = {
      this.proxyUser = user
      this
    }

    def setStartDateMills(startDateMills: Long): Builder = {
      this.startDateMills = startDateMills
      this
    }

    def setEndDateMills(endDateMills: Long): Builder = {
      this.endDateMills = endDateMills
      this
    }

    def addStatus(status: String): Builder = {
      if (StringUtils.isNotBlank(status)) {
        this.statusList += status.trim
      }
      this
    }

    def getStatusList(): ArrayBuffer[String] = this.statusList

    def setPageNow(pageNow: Int): Builder = {
      this.pageNow = pageNow
      this
    }

    def setPageSize(pageSize: Int): Builder = {
      this.pageSize = pageSize
      this
    }

    def setTaskID(taskID: Long): Builder = {
      this.taskID = taskID
      this
    }

    def setExecuteApplicationName(applicationName: String): Builder = {
      this.executeApplicationName = applicationName
      this
    }

    def build(): JobListAction = {
      val jobListAction = new JobListAction
      if (StringUtils.isNotBlank(proxyUser)) jobListAction.setParameter("proxyUser", proxyUser)
      if (startDateMills > 0) jobListAction.setParameter("startDate", startDateMills)
      if (endDateMills > 0) jobListAction.setParameter("endDate", endDateMills)
      if (!statusList.isEmpty) {
        jobListAction.setParameter("status", statusList.mkString(","))
      }
      if (pageNow > 0) jobListAction.setParameter("pageNow", pageNow)
      if (pageSize > 0) jobListAction.setParameter("pageSize", pageSize)
      if (taskID > 0) jobListAction.setParameter("taskID", taskID)
      if (StringUtils.isNotBlank(executeApplicationName)) jobListAction.setParameter("executeApplicationName", executeApplicationName)
      jobListAction
    }

  }

}


