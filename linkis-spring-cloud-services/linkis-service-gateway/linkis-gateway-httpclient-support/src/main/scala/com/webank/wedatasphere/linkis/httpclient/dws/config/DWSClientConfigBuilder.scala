/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.httpclient.dws.config

import com.webank.wedatasphere.linkis.httpclient.config.ClientConfigBuilder
import com.webank.wedatasphere.linkis.httpclient.dws.exception.UnknownVersionException
import org.apache.commons.lang.StringUtils

/**
  * created by cooperyang on 2019/5/27.
  */
class DWSClientConfigBuilder private() extends ClientConfigBuilder {

  private var dwsVersion: String = _

  def setDWSVersion(dwsVersion: String): this.type = {
    this.dwsVersion = dwsVersion
    this
  }

  override def build(): DWSClientConfig = {
    if(StringUtils.isBlank(dwsVersion)) throw new UnknownVersionException
    val clientConfig = new DWSClientConfig(super.build())
    clientConfig.setDWSVersion(dwsVersion)
    clientConfig
  }
}
object DWSClientConfigBuilder {
  def newBuilder(): DWSClientConfigBuilder = new DWSClientConfigBuilder
}