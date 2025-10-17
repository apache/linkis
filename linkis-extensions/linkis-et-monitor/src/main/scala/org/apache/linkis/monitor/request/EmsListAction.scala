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

import scala.collection.mutable.ArrayBuffer

class EmsListAction extends GetAction with MonitorAction {

  override def suffixURLs: Array[String] = Array("linkisManager", "listAllEMs")

}

object EmsListAction {
  def newBuilder(): Builder = new Builder

  class Builder private[EmsListAction] () {
    private var user: String = _
    private var instance: String = _
    private var nodeHealthy: String = _
    private var owner: String = _

    def setInstance(instance: String): Builder = {
      this.instance = instance
      this
    }

    def setNodeHealthy(nodeHealthy: String): Builder = {
      this.nodeHealthy = nodeHealthy
      this
    }

    def setOwner(owner: String): Builder = {
      this.owner = owner
      this
    }

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def build(): EmsListAction = {
      val emsListAction = new EmsListAction
      if (StringUtils.isNotBlank(instance)) emsListAction.setParameter("instance", instance)
      if (StringUtils.isNotBlank(nodeHealthy)) {
        emsListAction.setParameter("nodeHealthy", nodeHealthy)
      }
      if (StringUtils.isNotBlank(owner)) emsListAction.setParameter("owner", owner)
      if (StringUtils.isNotBlank(user)) emsListAction.setUser(user)
      emsListAction
    }

  }

}
