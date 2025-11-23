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

class OpenLogAction private extends GetAction with UJESJobAction {

  override def suffixURLs: Array[String] = Array("filesystem", "openLog")

}

object OpenLogAction {
  def newBuilder(): Builder = new Builder

  class Builder private[OpenLogAction] () {
    private var proxyUser: String = _
    private var logPath: String = _

    def setProxyUser(user: String): Builder = {
      this.proxyUser = user
      this
    }

    def setLogPath(path: String): Builder = {
      this.logPath = path
      this
    }

    def build(): OpenLogAction = {
      val openLogAction = new OpenLogAction
      openLogAction.setUser(proxyUser)
      openLogAction.setParameter("path", logPath)
      openLogAction
    }

  }

}
