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

package org.apache.linkis.rpc.sender

import org.springframework.beans.factory.ObjectProvider
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.loadbalancer.{
  DefaultResponse,
  EmptyResponse,
  Request,
  Response
}
import org.springframework.cloud.loadbalancer.core.{
  ReactorServiceInstanceLoadBalancer,
  ServiceInstanceListSupplier
}

import java.util

import scala.collection.JavaConverters._

import reactor.core.publisher.Mono

private class LinkisLoadBalancer(
    serviceInstance: ServiceInstance,
    serviceInstanceListSupplierProvider: ObjectProvider[ServiceInstanceListSupplier]
) extends ReactorServiceInstanceLoadBalancer {

  override def choose(request: Request[_]): Mono[Response[ServiceInstance]] = {
    val supplier: ServiceInstanceListSupplier = serviceInstanceListSupplierProvider.getIfAvailable()
    supplier.get(request).next.map(this.getInstanceResponse)
  }

  private def getInstanceResponse(
      instances: util.List[ServiceInstance]
  ): Response[ServiceInstance] = {
    if (instances.isEmpty) {
      new EmptyResponse
    }
    var instanceResult: ServiceInstance = null

    instances.asScala.find(instance =>
      serviceInstance.equals(instance.getInstanceId) &&
        serviceInstance.getHost.equals(instance.getHost) &&
        serviceInstance.getPort == instance.getPort
    ) match {
      case Some(instance) => instanceResult = instance
      case _ =>
    }

//    if (instanceResult == null) instanceResult = instances.get(0)
    if (instanceResult == null) {
      // 则重试

//      getOrRefresh(
//        refreshAllServers(),
//        getServiceInstances(serviceInstance.getApplicationName).contains(serviceInstance),
//        serviceInstance
//      )
    }
    new DefaultResponse(instanceResult)
  }

}
