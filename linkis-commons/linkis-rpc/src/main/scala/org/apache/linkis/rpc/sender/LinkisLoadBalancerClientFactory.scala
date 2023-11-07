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

import org.apache.linkis.common.ServiceInstance

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.client.DefaultServiceInstance
import org.springframework.cloud.client.loadbalancer.LoadBalancerClientsProperties
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory
import org.springframework.core.env.Environment

private class LinkisLoadBalancerClientFactory(
    serviceInstance: ServiceInstance,
    loadBalancerClientsProperties: LoadBalancerClientsProperties
) extends LoadBalancerClientFactory(loadBalancerClientsProperties: LoadBalancerClientsProperties) {

  @Autowired
  private var env: Environment = _

  @Autowired
  private var loadBalancerClientFactory: LoadBalancerClientFactory = _

  override def getInstance(
      serviceId: String
  ): ReactiveLoadBalancer[org.springframework.cloud.client.ServiceInstance] = {
    if (null != serviceInstance && StringUtils.isNotBlank(serviceInstance.getInstance)) {
      val hostAndPort: Array[String] = serviceInstance.getInstance.split(":")
      val defaultServiceInstance: DefaultServiceInstance = new DefaultServiceInstance(
        serviceInstance.getApplicationName,
        serviceId,
        hostAndPort.head,
        hostAndPort.last.toInt,
        true
      )
      val name: String = env.getProperty(LoadBalancerClientFactory.PROPERTY_NAME)
      new LinkisLoadBalancer(
        defaultServiceInstance,
        super.getLazyProvider(name, classOf[ServiceInstanceListSupplier])
      )
    } else {
      super.getInstance(serviceId)
    }

  }

}
