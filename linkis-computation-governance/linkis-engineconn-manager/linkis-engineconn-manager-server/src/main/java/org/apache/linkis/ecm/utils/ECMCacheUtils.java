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

package org.apache.linkis.ecm.utils;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class ECMCacheUtils {
  private static Cache<ServiceInstance, EngineStopRequest> ecStopRequestCache =
      CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();

  public static void putStopECToCache(
      ServiceInstance serviceInstance, EngineStopRequest engineStopRequest) {
    ecStopRequestCache.put(serviceInstance, engineStopRequest);
  }

  public static EngineStopRequest getStopEC(ServiceInstance serviceInstance) {
    return ecStopRequestCache.getIfPresent(serviceInstance);
  }
}
