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

package org.apache.linkis.rpc.message.method;

import org.apache.linkis.protocol.message.RequestProtocol;
import org.apache.linkis.rpc.message.exception.MessageWarnException;
import org.apache.linkis.rpc.message.parser.ServiceMethod;
import org.apache.linkis.rpc.message.registry.SpringServiceRegistry;
import org.apache.linkis.rpc.message.utils.MessageUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ReceiverMethodSearcher {

  /** key是requestProtocol的全类名，Map中，key是groupName */
  private final Map<String, Map<String, List<ServiceMethod>>> protocolServiceMethodCache =
      new ConcurrentHashMap<>();

  private SpringServiceRegistry serviceRegistry;

  private void initRegistry() {
    serviceRegistry = new SpringServiceRegistry();
  }

  public Map<String, List<MethodExecuteWrapper>> getMethodExecuteWrappers(
      RequestProtocol requestProtocol) {
    if (serviceRegistry == null) {
      synchronized (ReceiverMethodSearcher.class) {
        if (serviceRegistry == null) {
          initRegistry();
        }
      }
    }
    String protocolName = requestProtocol.getClass().getName();
    Map<String, List<ServiceMethod>> protocolServiceMethods =
        this.protocolServiceMethodCache.get(protocolName);
    if (protocolServiceMethods == null) {
      Map<String, List<ServiceMethod>> serviceMethodCache = serviceRegistry.getServiceMethodCache();
      Map<String, List<ServiceMethod>> serviceMatchs =
          serviceMethodCache.entrySet().stream()
              .filter(e -> MessageUtils.isAssignableFrom(e.getKey(), protocolName))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      serviceMatchs =
          serviceMatchs.values().stream()
              .flatMap(Collection::stream)
              .collect(Collectors.groupingBy(ServiceMethod::getChainName));
      // order判断
      for (List<ServiceMethod> value : serviceMatchs.values()) {
        Integer repeatOrder = MessageUtils.repeatOrder(value);
        if (repeatOrder != null && !MessageUtils.orderIsLast(repeatOrder, value)) {
          throw new MessageWarnException(
              10000, String.format("repeat order : %s for request %s", repeatOrder, protocolName));
        }
      }
      this.protocolServiceMethodCache.put(protocolName, serviceMatchs);
    }
    return serviceMethod2Wrapper(this.protocolServiceMethodCache.get(protocolName));
  }

  private Map<String, List<MethodExecuteWrapper>> serviceMethod2Wrapper(
      Map<String, List<ServiceMethod>> source) {
    HashMap<String, List<MethodExecuteWrapper>> target = new HashMap<>();
    source.forEach(
        (k, v) ->
            target.put(k, v.stream().map(MethodExecuteWrapper::new).collect(Collectors.toList())));
    return target;
  }
}
