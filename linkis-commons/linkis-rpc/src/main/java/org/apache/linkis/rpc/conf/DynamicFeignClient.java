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

package org.apache.linkis.rpc.conf;

import org.apache.linkis.DataWorkCloudApplication;

import org.apache.commons.lang3.StringUtils;

import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.cloud.openfeign.FeignClientFactoryBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class DynamicFeignClient<T> {

  private FeignClientBuilder feignClientBuilder;

  private final ConcurrentHashMap<String, T> CACHE_BEAN = new ConcurrentHashMap();

  public DynamicFeignClient() {
    this.feignClientBuilder =
        new FeignClientBuilder(DataWorkCloudApplication.getApplicationContext());
  }

  public T getFeignClient(final Class<T> type, final String serviceName) {
    return getFeignClient(type, serviceName, null);
  }

  public T getFeignClient(
      final Class<T> type, final Class<?> fallbackFactory, final String serviceName) {
    return getFeignClient(type, fallbackFactory, serviceName, null);
  }

  public T getFeignClient(
      final Class<T> type,
      final FeignClientFactoryBean clientFactoryBean,
      final String serviceName) {
    return getFeignClient(type, clientFactoryBean, serviceName, null);
  }

  public T getFeignClient(final Class<T> type, String serviceName, final String serviceUrl) {
    String k = serviceName;
    if (StringUtils.isNotEmpty(serviceUrl)) {
      k = serviceUrl;
    }
    return CACHE_BEAN.computeIfAbsent(
        k,
        (t) -> {
          FeignClientBuilder.Builder<T> builder =
              this.feignClientBuilder.forType(type, serviceName);
          if (StringUtils.isNotEmpty(serviceUrl)) {
            builder.url(serviceUrl);
          }
          return builder.build();
        });
  }

  public T getFeignClient(
      final Class<T> type,
      final Class<?> fallbackFactory,
      final String serviceName,
      final String serviceUrl) {
    String k = serviceName;
    if (StringUtils.isNotEmpty(serviceUrl)) {
      k = serviceUrl;
    }
    return CACHE_BEAN.computeIfAbsent(
        k,
        (t) -> {
          FeignClientFactoryBean feignClientFactoryBean = new FeignClientFactoryBean();
          feignClientFactoryBean.setFallbackFactory(fallbackFactory);
          FeignClientBuilder.Builder<T> builder =
              this.feignClientBuilder.forType(type, feignClientFactoryBean, serviceName);
          if (StringUtils.isNotEmpty(serviceUrl)) {
            builder.url(serviceUrl);
          }
          return builder.build();
        });
  }

  public T getFeignClient(
      final Class<T> type,
      final FeignClientFactoryBean clientFactoryBean,
      final String serviceName,
      final String serviceUrl) {
    String k = serviceName;
    if (StringUtils.isNotEmpty(serviceUrl)) {
      k = serviceUrl;
    }
    return CACHE_BEAN.computeIfAbsent(
        k,
        (t) -> {
          FeignClientBuilder.Builder<T> builder =
              this.feignClientBuilder.forType(type, clientFactoryBean, serviceName);
          if (StringUtils.isNotEmpty(serviceUrl)) {
            builder.url(serviceUrl);
          }
          return builder.build();
        });
  }

  private T getFromCache(final String serviceName, final String serviceUrl) {
    if (StringUtils.isNotEmpty(serviceUrl)) {
      return CACHE_BEAN.get(serviceUrl);
    } else {
      return CACHE_BEAN.get(serviceName);
    }
  }
}
