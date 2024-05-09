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

package org.apache.linkis.gateway.springcloud.http;

import org.apache.linkis.rpc.constant.RpcConstant;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class IpPriorityLoadBalancer implements ReactorServiceInstanceLoadBalancer {

  private static final Logger logger = LoggerFactory.getLogger(IpPriorityLoadBalancer.class);

  private final String serviceId;
  private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

  public IpPriorityLoadBalancer(
      String serviceId,
      ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider) {
    this.serviceId = serviceId;
    this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
  }

  @Override
  public Mono<Response<ServiceInstance>> choose(Request request) {
    List<String> clientIpList =
        ((RequestDataContext) request.getContext())
            .getClientRequest()
            .getHeaders()
            .get(RpcConstant.FIXED_INSTANCE);
    String clientIp = CollectionUtils.isNotEmpty(clientIpList) ? clientIpList.get(0) : null;
    ServiceInstanceListSupplier supplier =
        serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
    return supplier
        .get(request)
        .next()
        .map(serviceInstances -> processInstanceResponse(supplier, serviceInstances, clientIp));
  }

  private Response<ServiceInstance> processInstanceResponse(
      ServiceInstanceListSupplier supplier,
      List<ServiceInstance> serviceInstances,
      String clientIp) {
    Response<ServiceInstance> serviceInstanceResponse =
        getInstanceResponse(serviceInstances, clientIp);
    if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
      ((SelectedInstanceCallback) supplier)
          .selectedServiceInstance(serviceInstanceResponse.getServer());
    }
    return serviceInstanceResponse;
  }

  private Response<ServiceInstance> getInstanceResponse(
      List<ServiceInstance> instances, String clientIp) {
    if (instances.isEmpty()) {
      logger.warn("No servers available for service: " + serviceId);
      return new EmptyResponse();
    }
    if (StringUtils.isEmpty(clientIp)) {
      return new DefaultResponse(
          instances.get(ThreadLocalRandom.current().nextInt(instances.size())));
    }
    String[] ipAndPort = clientIp.split(":");
    if (ipAndPort.length != 2) {
      return new DefaultResponse(
          instances.get(ThreadLocalRandom.current().nextInt(instances.size())));
    }
    ServiceInstance chooseInstance = null;
    for (ServiceInstance instance : instances) {
      if (Objects.equals(ipAndPort[0], instance.getHost())
          && Objects.equals(ipAndPort[1], String.valueOf(instance.getPort()))) {
        return new DefaultResponse(instance);
      }
    }
    return new DefaultResponse(
        instances.get(ThreadLocalRandom.current().nextInt(instances.size())));
  }
}
