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

class EntranceTaskAction extends GetAction with MonitorAction {
  override def suffixURLs: Array[String] = Array("entrance/operation/metrics", "taskinfo")
}

object EntranceTaskAction {
  def newBuilder(): Builder = new Builder

  class Builder private[EntranceTaskAction] () {
    private var user: String = _
    private var creator: String = _
    private var engineTypeLabel: String = _
    private var instance: String = _

    def setCreator(creator: String): Builder = {
      this.creator = creator
      this
    }

    def setEngineTypeLabel(engineTypeLabel: String): Builder = {
      this.engineTypeLabel = engineTypeLabel
      this
    }

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setInstance(instance: String): Builder = {
      this.instance = instance
      this
    }

    def build(): EntranceTaskAction = {
      val entranceTaskAction = new EntranceTaskAction
      if (StringUtils.isNotBlank(creator)) entranceTaskAction.setParameter("creator", creator)
      if (StringUtils.isNotBlank(engineTypeLabel))
        entranceTaskAction.setParameter("engineTypeLabel", engineTypeLabel)
      if (StringUtils.isNotBlank(instance)) entranceTaskAction.setParameter("instance", instance)
      if (StringUtils.isNotBlank(user)) {
        // hadoop用户应该获取全部用户entrance信息，则无需传user，即可获取全部entrance信息
        if (user.equals("hadoop")) {
          entranceTaskAction.setParameter("user", "")
        } else {
          entranceTaskAction.setParameter("user", user)
        }
      }
      if (StringUtils.isNotBlank(user)) entranceTaskAction.setUser(user)
      entranceTaskAction
    }

  }

}
