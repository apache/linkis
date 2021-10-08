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

package com.apache.wedatasphere.linkis.gateway.ujes.route

import com.apache.wedatasphere.linkis.common.ServiceInstance
import com.apache.wedatasphere.linkis.gateway.config.GatewayConfiguration
import com.apache.wedatasphere.linkis.gateway.exception.TooManyServiceException
import com.apache.wedatasphere.linkis.gateway.http.GatewayContext
import com.apache.wedatasphere.linkis.gateway.route.AbstractGatewayRouter
import com.apache.wedatasphere.linkis.gateway.ujes.parser.EntranceExecutionGatewayParser
import com.apache.wedatasphere.linkis.protocol.constants.TaskConstant
import org.apache.commons.lang.StringUtils

class EntranceGatewayRouter extends AbstractGatewayRouter {

  protected def findEntranceService(parsedServiceId: String) = findService(parsedServiceId, list => {
    val services = list.filter(_.toLowerCase.contains("entrance"))
    if(services.length == 1) Some(services.head)
    else if(services.isEmpty) None
    else {
      val errorMsg = new TooManyServiceException(s"Cannot find a correct serviceId for parsedServiceId $parsedServiceId, service list is: " + services)
      warn("", errorMsg)
      throw errorMsg
    }
  })
  override def route(gatewayContext: GatewayContext): ServiceInstance = {
    gatewayContext.getGatewayRoute.getRequestURI match {
      case EntranceGatewayRouter.ENTRANCE_REGEX(_) =>
        val creator = gatewayContext.getGatewayRoute.getParams.get(TaskConstant.REQUESTAPPLICATIONNAME)
        val applicationName = gatewayContext.getGatewayRoute.getServiceInstance.getApplicationName
        //Ignore the router using application name
        if(applicationName.equals(GatewayConfiguration.ENTRANCE_SPRING_NAME.getValue)){
           return null
        }
        val serviceId = if(StringUtils.isBlank(creator) || creator == "IDE")
          findEntranceService(applicationName)
        else findEntranceService(creator).orElse {
          warn(s"Cannot find a service which named $creator, now redirect to $applicationName entrance.")
          findEntranceService(applicationName)
        }
        serviceId.map(ServiceInstance(_, gatewayContext.getGatewayRoute.getServiceInstance.getInstance)).orNull
      case _ => null
    }
  }
}
object EntranceGatewayRouter {
  val ENTRANCE_REGEX = (EntranceExecutionGatewayParser.ENTRANCE_HEADER + ".+").r
}
