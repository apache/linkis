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

import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.request.POSTAction

import java.util

class KillJobAction extends POSTAction with MonitorAction {

  private var execID: String = _

  override def suffixURLs: Array[String] = Array("entrance", execID, "killJobs")

  override def getRequestPayload: String =
    DWSHttpClient.jacksonJson.writeValueAsString(getRequestPayloads)

}

object KillJobAction {

  def builder(): Builder = new Builder

  class Builder private[KillJobAction] () {
    private var user: String = _

    private var idList: util.List[String] = _

    private var taskIDList: util.List[Long] = _

    private var execID: String = _

    def setIdList(idList: util.List[String]): Builder = {
      this.idList = idList
      this
    }

    def setTaskIDList(taskIDList: util.List[Long]): Builder = {
      this.taskIDList = taskIDList
      this
    }

    def setExecID(execID: String): Builder = {
      this.execID = execID
      this
    }

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def build(): KillJobAction = {
      val killJobAction = new KillJobAction
      killJobAction.setUser(user)
      killJobAction.addRequestPayload("idList", idList)
      killJobAction.addRequestPayload("taskIDList", taskIDList)
      killJobAction.execID = execID
      killJobAction
    }

  }

}
