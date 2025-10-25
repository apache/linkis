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
import org.apache.linkis.ujes.client.request.UJESJobAction

import org.apache.commons.lang3.StringUtils

class KeyvalueAction extends GetAction with MonitorAction {

  override def suffixURLs: Array[String] = Array("configuration", "keyvalue")

}

object KeyvalueAction {

  def newBuilder(): Builder = new Builder

  class Builder private[KeyvalueAction] () {

    private var engineType: String = _
    private var version: String = _
    private var creator: String = _
    private var configKey: String = _
    private var user: String = _

    def setEngineType(engineType: String): Builder = {
      this.engineType = engineType
      this
    }

    def setVersion(version: String): Builder = {
      this.version = version
      this
    }

    def setCreator(creator: String): Builder = {
      this.creator = creator
      this
    }

    def setConfigKey(configKey: String): Builder = {
      this.configKey = configKey
      this
    }

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def build(): KeyvalueAction = {
      val keyvalueAction = new KeyvalueAction
      if (StringUtils.isNotBlank(engineType)) keyvalueAction.setParameter("engineType", engineType)
      if (StringUtils.isNotBlank(version)) keyvalueAction.setParameter("version", version)
      if (StringUtils.isNotBlank(creator)) keyvalueAction.setParameter("creator", creator)
      if (StringUtils.isNotBlank(configKey)) keyvalueAction.setParameter("configKey", configKey)
      if (StringUtils.isNotBlank(user)) keyvalueAction.setUser(user)
      keyvalueAction
    }

  }

}
