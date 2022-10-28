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
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.gateway.config.GatewayConfiguration
import org.apache.linkis.gateway.errorcode.LinkisGatewayCoreErrorCodeSummary._
import org.apache.linkis.gateway.exception.GatewayErrorException
import org.apache.linkis.gateway.http.GatewayContext
import org.apache.linkis.gateway.route.AbstractGatewayRouter
import org.apache.linkis.instance.label.service.InsLabelService
import org.apache.linkis.manager.label.entity.route.RouteLabel
import org.apache.linkis.manager.label.utils.LabelUtils
import org.apache.linkis.rpc.interceptor.ServiceInstanceUtils

import org.apache.commons.lang3.StringUtils

import javax.annotation.Resource

import java.text.MessageFormat
import java.util

import scala.collection.JavaConverters._

abstract class AbstractLabelGatewayRouter extends AbstractGatewayRouter with Logging {

  @Resource
  private var insLabelService: InsLabelService = _

  /**
   * Add to the last of router chain
   *
   * @return
   */
  override def order(): Int = Int.MaxValue

  override def route(gatewayContext: GatewayContext): ServiceInstance = {
    val serviceInstance: ServiceInstance = gatewayContext.getGatewayRoute.getServiceInstance
    if (
        StringUtils.isNotBlank(serviceInstance.getApplicationName) && StringUtils.isNotBlank(
          serviceInstance.getInstance
        )
    ) {
      return serviceInstance
    }
    val applicationName = serviceInstance.getApplicationName
    if (!GatewayConfiguration.ROUTER_SERVER_LIST.getValue.contains(applicationName)) {
      // Ignore the router using application name
      return null
    }

    val routeLabels: util.List[RouteLabel] = parseToRouteLabels(gatewayContext)

    val canSelectInstances = if (null == routeLabels || routeLabels.isEmpty) {
      getDefaultInstances(applicationName)
    } else {
      val candidateServices = insLabelService.searchInstancesByLabels(routeLabels)
      if (null == candidateServices || candidateServices.isEmpty) {
        throw new GatewayErrorException(
          CANNOT_ROETE_SERVICE.getErrorCode,
          MessageFormat.format(
            CANNOT_ROETE_SERVICE.getErrorDesc,
            gatewayContext.getRequest.getRequestURI,
            LabelUtils.Jackson
              .toJson(routeLabels, null)
          )
        )
      } else {
        candidateServices
      }
    }

    val instance = selectInstance(gatewayContext, canSelectInstances)
    if (null == instance || StringUtils.isBlank(instance.getInstance)) {
      throw new GatewayErrorException(
        NO_SERVICES_REGISTRY.getErrorCode,
        MessageFormat.format(
          NO_SERVICES_REGISTRY.getErrorDesc,
          gatewayContext.getRequest.getRequestURI
        )
      )
    }
    instance
  }

  protected def getDefaultInstances(applicationName: String): util.List[ServiceInstance] = {
    val instances = ServiceInstanceUtils.getRPCServerLoader.getServiceInstances(applicationName)
    val allInstances = new util.ArrayList[ServiceInstance]()
    if (null != instances && instances.nonEmpty) allInstances.addAll(instances.toList.asJava)
    val labelInstances = insLabelService.getInstancesByNames(applicationName)
    allInstances.removeAll(labelInstances)
    allInstances
  }

  /**
   * Parse to route labels
   * @param gatewayContext
   *   context
   * @return
   */
  protected def parseToRouteLabels(gatewayContext: GatewayContext): util.List[RouteLabel]

  /**
   * Select instance
   * @param gatewayContext
   *   context
   * @param candidates
   *   candidate instances
   * @return
   */
  protected def selectInstance(
      gatewayContext: GatewayContext,
      candidates: util.List[ServiceInstance]
  ): ServiceInstance

}
