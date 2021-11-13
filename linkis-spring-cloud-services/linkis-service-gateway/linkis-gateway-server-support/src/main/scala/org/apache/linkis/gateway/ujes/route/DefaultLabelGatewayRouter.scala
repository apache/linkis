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
import java.util

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.gateway.exception.GatewayErrorException
import org.apache.linkis.gateway.http.GatewayContext
import org.apache.linkis.gateway.ujes.route.label.RouteLabelParser
import org.apache.linkis.manager.label.entity.route.RouteLabel
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConversions._
import scala.util.Random

class DefaultLabelGatewayRouter(var routeLabelParsers: util.List[RouteLabelParser]) extends AbstractLabelGatewayRouter {
  if(Option(routeLabelParsers).isEmpty){
     routeLabelParsers = new util.ArrayList[RouteLabelParser]()
  }
  override protected def parseToRouteLabels(gatewayContext: GatewayContext): util.List[RouteLabel] = {
    var routeLabels : Option[util.List[RouteLabel]] = None
    for (parser <- routeLabelParsers if routeLabels.isEmpty || routeLabels.get.isEmpty) {
      routeLabels = Option(parser.parse(gatewayContext))
    }
    routeLabels.getOrElse(new util.ArrayList[RouteLabel]())
  }

  override protected def selectInstance(gatewayContext: GatewayContext, candidates: util.List[ServiceInstance]): ServiceInstance = {
    if(candidates.size() <= 0){
      return null
    }
    if(gatewayContext.getGatewayRoute.getServiceInstance != null && StringUtils.isNotBlank(gatewayContext.getGatewayRoute.getServiceInstance.getApplicationName)){
      val applicationName:String =gatewayContext.getGatewayRoute.getServiceInstance.getApplicationName
      val filterCandidates =candidates.filter(serviceInstance => serviceInstance.getApplicationName.equalsIgnoreCase(applicationName))
      roulette(filterCandidates)
    }else {
      roulette(candidates)
    }
  }

  /**
   * Roulette to select service instance
   * @param serviceInstances instances
   * @return
   */
  private def roulette(serviceInstances: util.List[ServiceInstance]): ServiceInstance = {
    //Fetch from registry, make sure that the instances are available and the serviceId is right
    if(serviceInstances.size()<=0){
      throw new GatewayErrorException(11011, "ServiceInstances is empty, please check eureka service!")
    }
    val serviceId = serviceInstances.get(0).getApplicationName
    val filteredInstances = retainAllInRegistry(serviceId, serviceInstances)
    filteredInstances match {
      case _ if filteredInstances.size() == 1 => filteredInstances.get(0)
      case _ if filteredInstances.size() > 1 => filteredInstances.get(Random.nextInt(filteredInstances.size()))
      case _ => throw new GatewayErrorException(11012, s"Cannot find an instance in the routing chain of serviceId [" +
        serviceId + "], please retry")
    }
  }

}

