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

package com.webank.wedatasphere.linkis.gateway.security

import java.io.File
import java.util.Properties
import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.gateway.config.GatewayConfiguration._
import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.commons.lang.StringUtils

object ProxyUserUtils extends Logging {

  private val props = new Properties
  if(ENABLE_PROXY_USER.getValue){
    Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        info("loading proxy users.")
        val newProps = new Properties
        val file = new File(this.getClass.getClassLoader.getResource(PROXY_USER_CONFIG.getValue).toURI.getPath)
        val input = FileUtils.openInputStream(file)
        Utils.tryFinally(newProps.load(input))(IOUtils.closeQuietly(input))
        props.clear()
        props.putAll(newProps)
      }
    }, 0, PROXY_USER_SCAN_INTERVAL.getValue, TimeUnit.MILLISECONDS)
  }

  def getProxyUser(umUser: String): String = if(ENABLE_PROXY_USER.getValue) {
    val proxyUser = props.getProperty(umUser)
    if(StringUtils.isBlank(proxyUser)) umUser else {
      info(s"switched to proxy user $proxyUser for umUser $umUser.")
      proxyUser
    }
  } else umUser

}
