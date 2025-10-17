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
import org.apache.linkis.ujes.client.exception.UJESClientBuilderException

import org.apache.commons.lang3.StringUtils

class EncryptTokenAction extends GetAction with UJESJobAction {

  override def suffixURLs: Array[String] =
    Array("basedata-manager", "gateway-auth-token", "encrypt-token")

}

object EncryptTokenAction {
  def newBuilder(): Builder = new Builder

  class Builder private[EncryptTokenAction] () {
    private var user: String = _
    private var token: String = _

    def setToken(token: String): Builder = {
      this.token = token
      this
    }

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def build(): EncryptTokenAction = {
      val EncryptTokenAction = new EncryptTokenAction
      if (token == null) throw new UJESClientBuilderException("token is needed!")
      if (StringUtils.isNotBlank(token)) EncryptTokenAction.setParameter("token", token)
      if (StringUtils.isNotBlank(user)) EncryptTokenAction.setUser(user)
      EncryptTokenAction
    }

  }

}
