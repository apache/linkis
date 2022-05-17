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
 
package org.apache.linkis.gateway.ujes.parser

import org.apache.linkis.gateway.http.GatewayContext
import org.apache.linkis.gateway.parser.AbstractGatewayParser
import org.apache.linkis.gateway.ujes.parser.EntranceExecutionGatewayParser._
import org.apache.linkis.protocol.utils.ZuulEntranceUtils
import org.springframework.stereotype.Component

@Component
class EntranceRequestGatewayParser extends AbstractGatewayParser {
  override def shouldContainRequestBody(gatewayContext: GatewayContext): Boolean = false

  override def parse(gatewayContext: GatewayContext): Unit = gatewayContext.getRequest.getRequestURI match {
    case EntranceRequestGatewayParser.ENTRANCE_REQUEST_REGEX(version, execId) =>
      if (sendResponseWhenNotMatchVersion(gatewayContext, version)) return
      val serviceInstances = ZuulEntranceUtils.parseServiceInstanceByExecID(execId)
      gatewayContext.getGatewayRoute.setServiceInstance(serviceInstances(0))
    case _ =>
  }
}

object EntranceRequestGatewayParser {
  val ENTRANCE_REQUEST_REGEX = (ENTRANCE_HEADER + "([^/]+)/.+").r
}