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

package org.apache.linkis.ujes.client.request

import org.apache.linkis.httpclient.request.GetAction

import org.apache.commons.lang3.StringUtils

import scala.collection.mutable.ArrayBuffer

class JobListAction extends GetAction with UJESJobAction {

  override def suffixURLs: Array[String] = Array("jobhistory", "list")

}

object JobListAction {
  def newBuilder(): Builder = new Builder

  class Builder private[JobListAction] () {
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
      if (StringUtils.isNotBlank(executeApplicationName)) {
        jobListAction.setParameter("executeApplicationName", executeApplicationName)
      }
      jobListAction
    }

  }

}
