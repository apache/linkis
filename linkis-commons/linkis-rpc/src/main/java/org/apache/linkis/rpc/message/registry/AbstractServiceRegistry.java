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

package org.apache.linkis.rpc.message.registry;

import org.apache.linkis.rpc.message.parser.DefaultServiceParser;
import org.apache.linkis.rpc.message.parser.ServiceMethod;
import org.apache.linkis.rpc.message.parser.ServiceParser;

import org.springframework.aop.support.AopUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractServiceRegistry implements ServiceRegistry {

  private static final Logger logger = LoggerFactory.getLogger(AbstractServiceRegistry.class);

  @SuppressWarnings("all")
  public final Interner<String> lock = Interners.<String>newWeakInterner();
  /** key:requestprotocol or custom implicit object class name */
  private final Map<String, List<ServiceMethod>> serviceMethodCache = new ConcurrentHashMap<>();

  private final Map<String, Object> registedServieMap = new ConcurrentHashMap<>();

  private final ServiceParser serviceParser = new DefaultServiceParser();

  @SuppressWarnings("all")
  @Override
  public void register(Object service) {
    String serviceName = AopUtils.getTargetClass(service).getName();
    synchronized (this.lock.intern(serviceName)) {
      // 1.是否注册过
      Object o = this.registedServieMap.get(serviceName);
      if (o != null) return;
      // 2..解析
      Map<String, List<ServiceMethod>> serviceMethods = serviceParser.parse(service);
      serviceMethods.entrySet().stream()
          .filter(entry -> entry.getValue().size() != 1)
          .forEach(
              entry -> {
                logger.error(
                    String.format(
                        "rpc receive method init error! find %s method for the request:%s, this type of rpc request will not be handled!",
                        entry.getValue().size(), entry.getKey()));
              });
      // 3.注册
      serviceMethods.forEach(this::register);
      this.registedServieMap.put(serviceName, service);
    }
  }

  /**
   * @param key
   * @param value
   * @throws MessageWarnException
   */
  @SuppressWarnings("all")
  private void register(String key, List<ServiceMethod> serviceMethods) {
    // 防止相同key在不同service的并发注册
    synchronized (this.lock.intern(key)) {
      // 1.添加cache
      refreshServiceMethodCache(key, serviceMethods);
    }
  }

  private void refreshServiceMethodCache(String key, List<ServiceMethod> serviceMethods) {
    this.serviceMethodCache.computeIfAbsent(key, k -> new ArrayList<>()).addAll(serviceMethods);
  }

  public Map<String, List<ServiceMethod>> getServiceMethodCache() {
    return this.serviceMethodCache;
  }
}
