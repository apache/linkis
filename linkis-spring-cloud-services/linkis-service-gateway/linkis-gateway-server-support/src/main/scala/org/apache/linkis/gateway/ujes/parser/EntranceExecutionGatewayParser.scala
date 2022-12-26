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
import org.apache.linkis.gateway.springcloud.SpringCloudGatewayConfiguration._
import org.apache.linkis.gateway.ujes.route.label.RouteLabelParser
import org.apache.linkis.instance.label.service.InsLabelService
import org.apache.linkis.manager.label.entity.route.RouteLabel

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.Resource

import java.util

import scala.collection.JavaConverters._

@Component
class EntranceExecutionGatewayParser extends AbstractGatewayParser {

  @Autowired
  private var routeLabelParsers: util.List[RouteLabelParser] = _

  @Resource
  private var insLabelService: InsLabelService = _

  override def shouldContainRequestBody(gatewayContext: GatewayContext): Boolean =
    gatewayContext.getRequest.getRequestURI match {
      case EntranceExecutionGatewayParser.ENTRANCE_EXECUTION_REGEX(_, _) => true
      case _ => false
    }

  override def parse(gatewayContext: GatewayContext): Unit =
    gatewayContext.getRequest.getRequestURI match {
      case EntranceExecutionGatewayParser.ENTRANCE_EXECUTION_REGEX(version, _) =>
        if (sendResponseWhenNotMatchVersion(gatewayContext, version)) return
        gatewayContext.getGatewayRoute.setServiceInstance(
          ServiceInstance(GatewayConfiguration.ENTRANCE_SPRING_NAME.getValue, null)
        )
      case _ =>
    }

  protected def parseToRouteLabels(
      gatewayContext: GatewayContext
  ): Option[util.List[RouteLabel]] = {
    var routeLabels: Option[util.List[RouteLabel]] = None
    for (parser <- routeLabelParsers.asScala if routeLabels.isEmpty || routeLabels.get.isEmpty) {
      routeLabels = Option(parser.parse(gatewayContext))
    }
    routeLabels
  }

}

object EntranceExecutionGatewayParser {

  val ENTRANCE_HEADER =
    normalPath(API_URL_PREFIX) + "rest_[a-zA-Z][a-zA-Z_0-9]*/(v\\d+)/entrance/"

  val ENTRANCE_EXECUTION_REGEX =
    (ENTRANCE_HEADER + "(execute|backgroundservice|submit|killJobs)").r

}
