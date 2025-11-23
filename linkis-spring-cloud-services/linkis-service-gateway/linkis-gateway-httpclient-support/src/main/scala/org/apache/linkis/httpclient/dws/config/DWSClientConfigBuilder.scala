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

package org.apache.linkis.httpclient.dws.config

import org.apache.linkis.httpclient.config.ClientConfigBuilder
import org.apache.linkis.httpclient.dws.exception.UnknownVersionException

import org.apache.commons.lang3.StringUtils

class DWSClientConfigBuilder private () extends ClientConfigBuilder {

  private var dwsVersion: String = _

  def setDWSVersion(dwsVersion: String): this.type = {
    this.dwsVersion = dwsVersion
    this
  }

  override def build(): DWSClientConfig = {
    if (StringUtils.isBlank(dwsVersion)) throw new UnknownVersionException
    val clientConfig = new DWSClientConfig(super.build())
    clientConfig.setDWSVersion(dwsVersion)
    clientConfig
  }

}

object DWSClientConfigBuilder {
  def newBuilder(): DWSClientConfigBuilder = new DWSClientConfigBuilder
}
