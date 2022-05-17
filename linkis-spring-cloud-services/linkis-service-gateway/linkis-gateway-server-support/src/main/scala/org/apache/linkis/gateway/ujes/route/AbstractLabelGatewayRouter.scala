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
 
package org.apache.linkis.gateway.ujes.route

import org.apache.commons.lang.StringUtils
import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.gateway.http.GatewayContext
import org.apache.linkis.gateway.route.AbstractGatewayRouter
import org.apache.linkis.instance.label.service.InsLabelService
import org.apache.linkis.manager.label.entity.route.RouteLabel

import java.util
import javax.annotation.Resource

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
    val routeLabels: util.List[RouteLabel] = parseToRouteLabels(gatewayContext)
    val serviceInstance: ServiceInstance = gatewayContext.getGatewayRoute.getServiceInstance
    if (StringUtils.isNotBlank(serviceInstance.getApplicationName) && StringUtils.isNotBlank(serviceInstance.getInstance)) {
      return serviceInstance
    }
    // TODO: It's probably better to throw exception here
    if (null == routeLabels || routeLabels.isEmpty) {
      return null
    }

    val candidateServices = insLabelService.searchInstancesByLabels(routeLabels)
    val canSelectInstances = if (null == candidateServices || candidateServices.isEmpty) {
      val labelRelatedInstances = Option(insLabelService.searchLabelRelatedInstances(serviceInstance))
      removeAllFromRegistry(serviceInstance.getApplicationName,
        labelRelatedInstances.getOrElse(new util.ArrayList[ServiceInstance]()))
    } else {
      candidateServices
    }
    selectInstance(gatewayContext, canSelectInstances)
  }

  /**
   * Parse to route labels
   * @param gatewayContext context
   * @return
   */
  protected def parseToRouteLabels(gatewayContext: GatewayContext): util.List[RouteLabel]

  /**
   * Select instance
   * @param gatewayContext context
   * @param candidates candidate instances
   * @return
   */
  protected def selectInstance(gatewayContext: GatewayContext, candidates: util.List[ServiceInstance]): ServiceInstance
}
