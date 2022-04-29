/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.gateway.http

import org.apache.commons.lang3.StringUtils
import org.apache.linkis.gateway.config.GatewayConfiguration.{THIS_GATEWAY_SCHEMA, THIS_GATEWAY_URL}
import org.apache.linkis.rpc.Sender


object GatewayHttpUtils {

  private val GATEWAY_URL = "GATEWAY_URL"

  def addGateWayUrlToRequest(gatewayContext: GatewayContext): Unit = {
    val gatewWayURL = if (StringUtils.isNotBlank(THIS_GATEWAY_URL.getValue)) {
      THIS_GATEWAY_URL.getValue
    } else {
      val schema = if (StringUtils.isNotBlank(THIS_GATEWAY_SCHEMA.getValue)) {
        THIS_GATEWAY_SCHEMA.getValue
      } else {
        gatewayContext.getRequest.getURI.getScheme
      }
      s"$schema://${Sender.getThisInstance}"
    }
    val array = Array(gatewWayURL)
    gatewayContext.getRequest.addHeader(GATEWAY_URL, array)
  }
}
