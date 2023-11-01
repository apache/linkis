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

package org.apache.linkis.gateway.ujes.parser

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.gateway.config.GatewayConfiguration
import org.apache.linkis.gateway.http.GatewayContext
import org.apache.linkis.gateway.parser.AbstractGatewayParser
import org.apache.linkis.gateway.springcloud.SpringCloudGatewayConfiguration.{
  normalPath,
  API_URL_PREFIX
}
import org.apache.linkis.protocol.utils.ZuulEntranceUtils

import org.springframework.stereotype.Component

@Component
class ECMRequestGatewayParser extends AbstractGatewayParser {
  override def shouldContainRequestBody(gatewayContext: GatewayContext): Boolean = false

  override def parse(gatewayContext: GatewayContext): Unit = {
    logger.info("start begin  ECMRequestGatewayParser {}", gatewayContext)
    gatewayContext.getRequest.getRequestURI match {
      case ECMRequestGatewayParser.ECM_EXECUTION_REGEX(version, execId) =>
        if (sendResponseWhenNotMatchVersion(gatewayContext, version)) return
        val serviceInstance =
          if (
              gatewayContext.getRequest.getQueryParams.containsKey(ECMRequestGatewayParser.INSTANCE)
          ) {
            val instances =
              gatewayContext.getRequest.getQueryParams.get(ECMRequestGatewayParser.INSTANCE)
            if (null != instances && instances.length == 1) {
              ServiceInstance(
                GatewayConfiguration.ENGINECONN_MANAGER_SPRING_NAME.getValue,
                instances(0)
              )
            } else {
              ServiceInstance(GatewayConfiguration.ENGINECONN_MANAGER_SPRING_NAME.getValue, null)
            }
          } else {
            ServiceInstance(GatewayConfiguration.ENGINECONN_MANAGER_SPRING_NAME.getValue, null)
          }
        gatewayContext.getGatewayRoute.setServiceInstance(serviceInstance)
      case _ =>
    }
  }

}

object ECMRequestGatewayParser {

  val ECM_HEADER =
    normalPath(API_URL_PREFIX) + "rest_[a-zA-Z][a-zA-Z_0-9]*/(v\\d+)/engineconnManager/"

  val ECM_EXECUTION_REGEX =
    (ECM_HEADER + "(downloadEngineLog)").r

  val INSTANCE = "emInstance"

}
