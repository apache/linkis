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

package com.webank.wedatasphere.linkis.gateway.ujes.parser

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.gateway.config.GatewayConfiguration
import com.webank.wedatasphere.linkis.gateway.http.GatewayContext
import com.webank.wedatasphere.linkis.gateway.parser.AbstractGatewayParser
import com.webank.wedatasphere.linkis.gateway.springcloud.SpringCloudGatewayConfiguration._
import com.webank.wedatasphere.linkis.gateway.ujes.route.label.RouteLabelParser
import com.webank.wedatasphere.linkis.instance.label.service.InsLabelService
import com.webank.wedatasphere.linkis.manager.label.entity.route.RouteLabel
import javax.annotation.Resource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._

@Component
class EntranceExecutionGatewayParser extends AbstractGatewayParser {


  @Autowired
  private var routeLabelParsers: util.List[RouteLabelParser] = _

  @Resource
  private var insLabelService: InsLabelService = _

  override def shouldContainRequestBody(gatewayContext: GatewayContext): Boolean = gatewayContext.getRequest.getRequestURI match {
    case EntranceExecutionGatewayParser.ENTRANCE_EXECUTION_REGEX(_, _) => true
    case _ => false
  }

  override def parse(gatewayContext: GatewayContext): Unit = gatewayContext.getRequest.getRequestURI match {
    case EntranceExecutionGatewayParser.ENTRANCE_EXECUTION_REGEX(version, _) =>
      if (sendResponseWhenNotMatchVersion(gatewayContext, version)) return
      val routeLabelsOption = parseToRouteLabels(gatewayContext)
      val path = gatewayContext.getRequest.getRequestURI
      val applicationName = if (routeLabelsOption.isDefined && routeLabelsOption.get.nonEmpty) {
        val instances = insLabelService.searchInstancesByLabels(routeLabelsOption.get)
        if (instances.isEmpty) {
          GatewayConfiguration.ENTRANCE_SPRING_NAME.getValue
        } else {
          instances(0).getApplicationName
        }
      } else {
        GatewayConfiguration.ENTRANCE_SPRING_NAME.getValue
      }
      info(s"GatewayParser parse requestUri $path to service ${applicationName}.")
      gatewayContext.getGatewayRoute.setServiceInstance(ServiceInstance(applicationName, null))
    case _ =>
  }

  protected def parseToRouteLabels(gatewayContext: GatewayContext): Option[util.List[RouteLabel]] = {
    var routeLabels: Option[util.List[RouteLabel]] = None
    for (parser <- routeLabelParsers if routeLabels.isEmpty || routeLabels.get.isEmpty) {
      routeLabels = Option(parser.parse(gatewayContext))
    }
    routeLabels
  }
}

object EntranceExecutionGatewayParser {
  val ENTRANCE_HEADER = normalPath(API_URL_PREFIX) + "rest_[a-zA-Z][a-zA-Z_0-9]*/(v\\d+)/entrance/"
  val ENTRANCE_EXECUTION_REGEX = (ENTRANCE_HEADER + "(execute|backgroundservice|submit|killJobs)").r
}