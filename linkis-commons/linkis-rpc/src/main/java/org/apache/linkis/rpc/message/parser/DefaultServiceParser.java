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

package org.apache.linkis.rpc.message.parser;

import org.apache.linkis.rpc.Sender;
import org.apache.linkis.rpc.message.annotation.Chain;
import org.apache.linkis.rpc.message.annotation.Order;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultServiceParser implements ServiceParser {

  Logger logger = LoggerFactory.getLogger(DefaultServiceParser.class);

  @Override
  public Map<String, List<ServiceMethod>> parse(Object service) {
    // TODO: 2020/7/15 more analysis
    //        Method[] methods = service.getClass().getMethods();
    Method[] methods = AopUtils.getTargetClass(service).getMethods();
    return Arrays.stream(methods)
        .filter(this::methodFilterPredicate)
        .map(m -> getServiceMethod(m, service))
        .collect(Collectors.groupingBy(ServiceMethod::getProtocolName));
  }

  private ServiceMethod getServiceMethod(Method method, Object service) {
    Parameter[] parameters = method.getParameters();
    if (parameters.length == 0) {
      return null;
    }
    Parameter protocolParameter = parameters[0];

    ServiceMethod serviceMethod = new ServiceMethod();
    serviceMethod.setMethod(method);
    serviceMethod.setService(service);
    serviceMethod.setAlias(String.format("%s.%s", service.getClass().getName(), method.getName()));
    Order order = method.getAnnotation(Order.class);
    if (order != null) {
      serviceMethod.setOrder(order.value());
    }
    Chain chain = method.getAnnotation(Chain.class);
    if (chain != null) {
      serviceMethod.setChainName(chain.value());
    }

    if (parameters.length == 2) {
      serviceMethod.setHasSender(true);
      if (Sender.class.isAssignableFrom(parameters[0].getType())) {
        serviceMethod.setSenderOnLeft(true);
        protocolParameter = parameters[1];
      }
    }
    logger.info(
        method
            + " parameter:"
            + Arrays.toString(Arrays.stream(parameters).map(Parameter::getName).toArray()));
    serviceMethod.setProtocolName(protocolParameter.getType().getName());
    return serviceMethod;
  }

  private boolean methodFilterPredicate(Method method) {
    if (method.getAnnotation(Receiver.class) != null) {
      Class<?>[] parameterTypes = method.getParameterTypes();
      if (method.getParameterCount() == 1) {
        return !Sender.class.isAssignableFrom(parameterTypes[0]);
      } else if (method.getParameterCount() == 2) {
        boolean hasContext = Arrays.stream(parameterTypes).anyMatch(Sender.class::isAssignableFrom);
        boolean allContext = Arrays.stream(parameterTypes).allMatch(Sender.class::isAssignableFrom);
        return hasContext && !allContext;
      }
    }
    return false;
  }
}
