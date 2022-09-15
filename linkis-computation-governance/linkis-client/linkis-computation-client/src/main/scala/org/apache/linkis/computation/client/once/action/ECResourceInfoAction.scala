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

class ECResourceInfoAction extends GetAction with LinkisManagerAction {
  override def suffixURLs: Array[String] = Array("linkisManager", "ecinfo/get")
}

object ECResourceInfoAction {
  def newBuilder(): Builder = new Builder

  class Builder private[ECResourceInfoAction] () {

    private var user: String = _

    private var ticketid: String = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setTicketid(ticketid: String): Builder = {
      this.ticketid = ticketid
      this
    }

    def build(): ECResourceInfoAction = {
      if (user == null) throw new UJESClientBuilderException("user is needed!")
      val ecResourceInfoAction = new ECResourceInfoAction
      ecResourceInfoAction.setUser(user)
      ecResourceInfoAction.setParameter("ticketid", ticketid)
      ecResourceInfoAction
    }

  }

}
