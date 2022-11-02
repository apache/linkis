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

package org.apache.linkis.rpc.message.utils;

import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;

import java.lang.reflect.Field;

import feign.Request.Options;

public class LoadBalancerOptionsUtils {

  private static Options DEFAULT_OPTIONS = null;

  private static Object locker = new Object();

  public static Options getDefaultOptions() throws NoSuchFieldException, IllegalAccessException {
    if (null == DEFAULT_OPTIONS) {
      synchronized (locker) {
        Class<?> clazz = LoadBalancerFeignClient.class;
        Field optionField = clazz.getDeclaredField("DEFAULT_OPTIONS");
        optionField.setAccessible(true);
        Object o = optionField.get(clazz);
        DEFAULT_OPTIONS = (Options) o;
      }
    }
    return DEFAULT_OPTIONS;
  }
}
