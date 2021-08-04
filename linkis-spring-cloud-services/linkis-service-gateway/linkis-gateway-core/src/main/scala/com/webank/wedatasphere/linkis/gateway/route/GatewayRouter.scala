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

package com.webank.wedatasphere.linkis.gateway.route

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.gateway.exception.TooManyServiceException
import com.webank.wedatasphere.linkis.gateway.http.GatewayContext
import com.webank.wedatasphere.linkis.rpc.interceptor.ServiceInstanceUtils
import com.webank.wedatasphere.linkis.rpc.sender.SpringCloudFeignConfigurationCache
import com.webank.wedatasphere.linkis.server.Message
import com.webank.wedatasphere.linkis.server.exception.NoApplicationExistsException
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.exception.ExceptionUtils

trait GatewayRouter {

  def route(gatewayContext: GatewayContext): ServiceInstance

  def order():Int = -1

}
abstract class AbstractGatewayRouter extends GatewayRouter with Logging {
  import scala.collection.JavaConversions._
  protected def findAndRefreshIfNotExists(serviceId: String, findService: => Option[String]): Option[String] = {
    var service = findService
    if(service.isEmpty) {
      val applicationNotExists = new NoApplicationExistsException(10050, "Application " +
        serviceId + " is not exists any instances.")
      Utils.tryThrow(Utils.waitUntil(() =>{
        ServiceInstanceUtils.refreshServiceInstances()
        service = findService
        service.nonEmpty
      }, ServiceInstanceUtils.serviceRefreshMaxWaitTime(), 500, 2000)){ t =>
        warn(s"Need a random $serviceId instance, but no one can find in Discovery refresh.", t)
        applicationNotExists.initCause(t)
        applicationNotExists
      }
    }
    service
  }
  protected def findService(parsedServiceId: String, tooManyDeal: List[String] => Option[String]): Option[String] = {
    val services = SpringCloudFeignConfigurationCache.getDiscoveryClient
      .getServices.filter(_.toLowerCase.contains(parsedServiceId.toLowerCase)).toList
    if(services.length == 1) Some(services.head)
    else if(services.length > 1) tooManyDeal(services)
    else None
  }

  protected def retainAllInRegistry(serviceId: String, serviceInstances: util.List[ServiceInstance]): util.List[ServiceInstance] ={
    val instancesInRegistry = SpringCloudFeignConfigurationCache.getDiscoveryClient
      .getInstances(serviceId).map( instance => instance.getHost + ":" + instance.getPort)
    serviceInstances.filter(instance => {
      instancesInRegistry.contains(instance.getInstance)
    })
  }

  protected def removeAllFromRegistry(serviceId: String, serviceInstances: util.List[ServiceInstance]): util.List[ServiceInstance] = {
    var serviceInstancesInRegistry = SpringCloudFeignConfigurationCache.getDiscoveryClient
      .getInstances(serviceId).map( instance =>
      ServiceInstance(serviceId, instance.getHost + ":" + instance.getPort)
    )
    serviceInstances.foreach(serviceInstance => {
      serviceInstancesInRegistry = serviceInstancesInRegistry.filterNot(_.equals(serviceInstance))
    })
    serviceInstancesInRegistry
  }
}

class DefaultGatewayRouter(var gatewayRouters: Array[GatewayRouter]) extends AbstractGatewayRouter{

  gatewayRouters = gatewayRouters.sortWith((left, right) => {
    left.order() < right.order()
  })

  private def findCommonService(parsedServiceId: String) = findService(parsedServiceId, services => {
    val errorMsg = new TooManyServiceException(s"Cannot find a correct serviceId for parsedServiceId $parsedServiceId, service list is: " + services)
    warn("", errorMsg)
    throw errorMsg
  })

  protected def findReallyService(gatewayContext: GatewayContext): ServiceInstance = {
    var serviceInstance: ServiceInstance = null
    for (router <- gatewayRouters if serviceInstance == null) {
      serviceInstance = router.route(gatewayContext)
    }
    if(serviceInstance == null) serviceInstance = gatewayContext.getGatewayRoute.getServiceInstance
    val service = findAndRefreshIfNotExists(serviceInstance.getApplicationName,
      findCommonService(serviceInstance.getApplicationName))
    service.map { applicationName =>
      if(StringUtils.isNotBlank(serviceInstance.getInstance)) {
        val _serviceInstance = ServiceInstance(applicationName, serviceInstance.getInstance)
        ServiceInstanceUtils.getRPCServerLoader.getOrRefreshServiceInstance(_serviceInstance)
        _serviceInstance
      } else ServiceInstance(applicationName, null)
    }.get
  }
  override def route(gatewayContext: GatewayContext): ServiceInstance = if(gatewayContext.getGatewayRoute.getServiceInstance != null) {
    val parsedService = gatewayContext.getGatewayRoute.getServiceInstance.getApplicationName
    val serviceInstance = Utils.tryCatch(findReallyService(gatewayContext)){ t =>
      val message = Message.error(ExceptionUtils.getRootCauseMessage(t)) << gatewayContext.getRequest.getRequestURI
      message.data("data", gatewayContext.getRequest.getRequestBody)
      warn("", t)
      if(gatewayContext.isWebSocketRequest) gatewayContext.getResponse.writeWebSocket(message) else gatewayContext.getResponse.write(message)
      gatewayContext.getResponse.sendResponse()
      return null
    }
    info("GatewayRouter route requestUri " + gatewayContext.getGatewayRoute.getRequestURI + " with parsedService " + parsedService + " to " + serviceInstance)
    serviceInstance
  } else null
}