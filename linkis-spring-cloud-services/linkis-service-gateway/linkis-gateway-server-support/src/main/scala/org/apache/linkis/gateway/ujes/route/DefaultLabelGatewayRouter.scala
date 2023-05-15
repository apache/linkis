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

package org.apache.linkis.gateway.ujes.route

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.gateway.errorcode.LinkisGatewayCoreErrorCodeSummary._
import org.apache.linkis.gateway.exception.GatewayErrorException
import org.apache.linkis.gateway.http.GatewayContext
import org.apache.linkis.gateway.parser.RouteLabelParser
import org.apache.linkis.manager.label.entity.route.RouteLabel

import org.apache.commons.lang3.StringUtils

import java.text.MessageFormat
import java.util

import scala.collection.JavaConverters._
import scala.util.Random

class DefaultLabelGatewayRouter(var routeLabelParsers: util.List[RouteLabelParser])
    extends AbstractLabelGatewayRouter {

  if (Option(routeLabelParsers).isEmpty) {
    routeLabelParsers = new util.ArrayList[RouteLabelParser]()
  }

  override protected def parseToRouteLabels(
      gatewayContext: GatewayContext
  ): util.List[RouteLabel] = {
    var routeLabels: Option[util.List[RouteLabel]] = None
    for (parser <- routeLabelParsers.asScala if routeLabels.isEmpty || routeLabels.get.isEmpty) {
      routeLabels = Option(parser.parse(gatewayContext))
    }
    routeLabels.getOrElse(new util.ArrayList[RouteLabel]())
  }

  override protected def selectInstance(
      gatewayContext: GatewayContext,
      candidates: util.List[ServiceInstance]
  ): ServiceInstance = {
    if (candidates.size() <= 0) {
      return null
    }
    if (
        gatewayContext.getGatewayRoute.getServiceInstance != null && StringUtils.isNotBlank(
          gatewayContext.getGatewayRoute.getServiceInstance.getApplicationName
        )
    ) {
      val applicationName: String =
        gatewayContext.getGatewayRoute.getServiceInstance.getApplicationName
      val filterCandidates = candidates.asScala.filter(serviceInstance =>
        serviceInstance.getApplicationName.equalsIgnoreCase(applicationName)
      )
      roulette(filterCandidates.asJava)
    } else {
      roulette(candidates)
    }
  }

  /**
   * Roulette to select service instance
   * @param serviceInstances
   *   instances
   * @return
   */
  private def roulette(serviceInstances: util.List[ServiceInstance]): ServiceInstance = {
    // Fetch from registry, make sure that the instances are available and the serviceId is right
    if (serviceInstances.size() <= 0) {
      throw new GatewayErrorException(NO_ROUTE_SERVICE.getErrorCode, NO_ROUTE_SERVICE.getErrorDesc)
    }

    val serviceIds = serviceInstances.asScala.map(_.getApplicationName).distinct
    val filteredInstances = new util.ArrayList[ServiceInstance]()
    for (serviceId <- serviceIds) {
      filteredInstances.addAll(retainAllInRegistry(serviceId, serviceInstances))
    }

    if (filteredInstances.size() > 0) {
      filteredInstances.get(Random.nextInt(filteredInstances.size()))
    } else {
      throw new GatewayErrorException(
        CANNOT_INSTANCE.getErrorCode,
        MessageFormat.format(CANNOT_INSTANCE.getErrorDesc, serviceIds.mkString(","))
      )
    }
  }

}
