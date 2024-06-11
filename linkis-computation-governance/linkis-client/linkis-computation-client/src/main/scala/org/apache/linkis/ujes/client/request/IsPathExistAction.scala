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

class IsPathExistAction extends GetAction with UJESJobAction {

  override def suffixURLs: Array[String] = Array("filesystem", "isExist")
}

object IsPathExistAction {
  def builder(): Builder = new Builder

  class Builder private[IsPathExistAction] () {
    private var user: String = _
    private var path: String = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setPath(path: String): Builder = {
      this.path = path
      this
    }

    def build(): IsPathExistAction = {
      val isPathExistAction = new IsPathExistAction
      if (user == null) throw new UJESClientBuilderException("user is needed!")
      if (path == null) throw new UJESClientBuilderException("path is needed!")
      isPathExistAction.setUser(user)
      isPathExistAction.setParameter("path", path)
      isPathExistAction
    }

  }

}
