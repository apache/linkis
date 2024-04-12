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

package org.apache.linkis.rpc.loadbalancer;

import org.apache.linkis.rpc.conf.CacheManualRefresher;
import org.apache.linkis.rpc.conf.RPCConfiguration;
import org.apache.linkis.rpc.constant.RpcConstant;
import org.apache.linkis.rpc.errorcode.LinkisRpcErrorCodeSummary;
import org.apache.linkis.rpc.exception.NoInstanceExistsException;
import org.apache.linkis.rpc.sender.SpringCloudFeignConfigurationCache$;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import reactor.core.publisher.Mono;

public class ServiceInstancePriorityLoadBalancer implements ReactorServiceInstanceLoadBalancer {

  private static final Log log = LogFactory.getLog(ServiceInstancePriorityLoadBalancer.class);

  @Autowired private CacheManualRefresher cacheManualRefresher;

  private final String serviceId;

  final AtomicInteger position;
  private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

  private final Long maxWaitTime = RPCConfiguration.RPC_SERVICE_REFRESH_MAX_WAIT_TIME().getValue().toLong();

  public ServiceInstancePriorityLoadBalancer(
      ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
      String serviceId) {
    this(serviceInstanceListSupplierProvider, serviceId, (new Random()).nextInt(1000));
  }

  public ServiceInstancePriorityLoadBalancer(
      ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
      String serviceId,
      int seedPosition) {
    this.serviceId = serviceId;
    this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
    this.position = new AtomicInteger(seedPosition);
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
        .map(
            serviceInstances ->
                processInstanceResponse(request, supplier, serviceInstances, clientIp));
  }

  private Response<ServiceInstance> processInstanceResponse(
      Request request,
      ServiceInstanceListSupplier supplier,
      List<ServiceInstance> serviceInstances,
      String clientIp) {
    Response<ServiceInstance> serviceInstanceResponse =
        getInstanceResponse(serviceInstances, clientIp);
    Long endTime = System.currentTimeMillis() + maxWaitTime;

    List<String> linkisLoadBalancerTypeList =
        ((RequestDataContext) request.getContext())
            .getClientRequest()
            .getHeaders()
            .get(RpcConstant.LINKIS_LOAD_BALANCER_TYPE);
    String linkisLoadBalancerType =
        CollectionUtils.isNotEmpty(linkisLoadBalancerTypeList)
            ? linkisLoadBalancerTypeList.get(0)
            : null;

    while (null == serviceInstanceResponse
        && StringUtils.isNotBlank(clientIp)
        && isRPC(linkisLoadBalancerType)
        && System.currentTimeMillis() < endTime) {
      cacheManualRefresher.refresh();
      List<ServiceInstance> instances =
          SpringCloudFeignConfigurationCache$.MODULE$.discoveryClient().getInstances(serviceId);
      serviceInstanceResponse = getInstanceResponse(instances, clientIp);
      if (null == serviceInstanceResponse) {
        try {
          Thread.sleep(5000L);
        } catch (InterruptedException e) {

        }
      }
    }

    if (null == serviceInstanceResponse && StringUtils.isNotBlank(clientIp)) {
      throw new NoInstanceExistsException(
          LinkisRpcErrorCodeSummary.INSTANCE_NOT_FOUND_ERROR.getErrorCode(),
          MessageFormat.format(LinkisRpcErrorCodeSummary.INSTANCE_NOT_FOUND_ERROR.getErrorDesc(), clientIp));
    }

    if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
      ((SelectedInstanceCallback) supplier)
          .selectedServiceInstance(serviceInstanceResponse.getServer());
    }
    return serviceInstanceResponse;
  }

  private boolean isRPC(String linkisLoadBalancerType) {
    return StringUtils.isNotBlank(linkisLoadBalancerType)
        && linkisLoadBalancerType.equalsIgnoreCase(RpcConstant.LINKIS_LOAD_BALANCER_TYPE_RPC);
  }

  private Response<ServiceInstance> getInstanceResponse(
      List<ServiceInstance> instances, String clientIp) {
    if (instances.isEmpty()) {
      log.warn("No servers available for service: " + serviceId);
      return null;
    }
    int pos = this.position.incrementAndGet() & Integer.MAX_VALUE;

    if (StringUtils.isBlank(clientIp)) {
      return new DefaultResponse(instances.get(pos % instances.size()));
    }
    String[] ipAndPort = clientIp.split(":");
    if (ipAndPort.length != 2) {
      throw new NoInstanceExistsException(
          LinkisRpcErrorCodeSummary.INSTANCE_ERROR.getErrorCode(),
          MessageFormat.format(LinkisRpcErrorCodeSummary.INSTANCE_ERROR.getErrorDesc(), clientIp));
    }
    ServiceInstance chooseInstance = null;
    for (ServiceInstance instance : instances) {
      if (Objects.equals(ipAndPort[0], instance.getHost())
          && Objects.equals(ipAndPort[1], String.valueOf(instance.getPort()))) {
        chooseInstance = instance;
        break;
      }
    }
    if (null == chooseInstance) {
      return null;
    } else {
      return new DefaultResponse(chooseInstance);
    }
  }
}
