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

package org.apache.linkis.computation.client.once.action

import org.apache.linkis.httpclient.request.GetAction
import org.apache.linkis.ujes.client.exception.UJESClientBuilderException

class ListEngineConnAction extends GetAction with LinkisManagerAction {
  override def suffixURLs: Array[String] = Array("linkisManager", "listUserEngines")
}

object ListEngineConnAction {
  def newBuilder(): Builder = new Builder

  class Builder private[ListEngineConnAction] () {

    private var user: String = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def build(): ListEngineConnAction = {
      if (user == null) throw new UJESClientBuilderException("user is needed!")
      val listEngineConnAction = new ListEngineConnAction
      listEngineConnAction.setUser(user)
      listEngineConnAction
    }

  }

}
