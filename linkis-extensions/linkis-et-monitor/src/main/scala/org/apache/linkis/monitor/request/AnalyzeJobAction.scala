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

package org.apache.linkis.monitor.request

import org.apache.linkis.httpclient.request.GetAction

import org.apache.commons.lang3.StringUtils

class AnalyzeJobAction extends GetAction with MonitorAction {

  override def suffixURLs: Array[String] = Array("jobhistory", "diagnosis-query")

}

object AnalyzeJobAction {

  def newBuilder(): Builder = new Builder

  class Builder private[AnalyzeJobAction] () {

    private var taskID: String = _
    private var user: String = _

    def setTaskID(taskID: String): Builder = {
      this.taskID = taskID
      this
    }

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def build(): AnalyzeJobAction = {
      val analyzeJobAction = new AnalyzeJobAction
      if (StringUtils.isNotBlank(taskID)) analyzeJobAction.setParameter("taskID", taskID)
      if (StringUtils.isNotBlank(user)) analyzeJobAction.setUser(user)
      analyzeJobAction
    }

  }

}
