/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.gateway.ujes.route

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.gateway.exception.GatewayErrorException
import com.webank.wedatasphere.linkis.gateway.http.GatewayContext
import com.webank.wedatasphere.linkis.gateway.route.AbstractGatewayRouter
import com.webank.wedatasphere.linkis.instance.label.service.InsLabelService
import com.webank.wedatasphere.linkis.manager.label.entity.route.RouteLabel
import javax.annotation.Resource
import org.apache.commons.lang.StringUtils

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
    val routeLabels: Option[util.List[RouteLabel]] = Option(parseToRouteLabels(gatewayContext))
    val serviceInstance: ServiceInstance = gatewayContext.getGatewayRoute.getServiceInstance
    if (StringUtils.isNotBlank(serviceInstance.getApplicationName) && StringUtils.isNotBlank(serviceInstance.getInstance)) {
      //如果前面已经确认instance则跳过
      return serviceInstance
    }
    val candidateServices = routeLabels match {
      case _ if routeLabels.isDefined && !routeLabels.get.isEmpty =>
        gatewayContext.getGatewayRoute.setLabels(routeLabels.get)
        Option(insLabelService.searchInstancesByLabels(routeLabels.get))
      case _ =>
        if (null != serviceInstance) {
          warn("Cannot find route labels, now to find the default [" + serviceInstance.getApplicationName + "] service instances")
          //Use application name to query service instances
          Option(insLabelService.searchUnRelateInstances(serviceInstance))
        } else {
          throw new GatewayErrorException(11011, s"Cannot route the service instance without application name and labels")
        }
    }
    val canSelectInstances = if (candidateServices.isEmpty || candidateServices.get.size <= 0) {
      val labelRelatedInstances = Option(insLabelService.searchLabelRelatedInstances(serviceInstance))
      removeAllFromRegistry(serviceInstance.getApplicationName,
        labelRelatedInstances.getOrElse(new util.ArrayList[ServiceInstance]()))
    } else {
      candidateServices.get
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
