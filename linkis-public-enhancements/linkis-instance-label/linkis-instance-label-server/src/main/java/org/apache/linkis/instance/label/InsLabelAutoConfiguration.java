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

package org.apache.linkis.instance.label;

import org.apache.linkis.instance.label.service.InsLabelAccessService;
import org.apache.linkis.instance.label.service.InsLabelServiceAdapter;
import org.apache.linkis.instance.label.service.annotation.AdapterMode;
import org.apache.linkis.instance.label.service.impl.DefaultInsLabelService;
import org.apache.linkis.instance.label.service.impl.DefaultInsLabelServiceAdapter;
import org.apache.linkis.instance.label.service.impl.EurekaInsLabelService;
import org.apache.linkis.mybatis.DataSourceConfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class InsLabelAutoConfiguration {
  private static final Logger LOG = LoggerFactory.getLogger(InsLabelAutoConfiguration.class);

  @ConditionalOnClass({DataSourceConfig.class})
  @ConditionalOnMissingBean({DefaultInsLabelService.class})
  @Bean
  @Scope("prototype")
  public InsLabelAccessService defaultInsLabelService() {
    return new DefaultInsLabelService();
  }

  @ConditionalOnMissingBean({InsLabelServiceAdapter.class})
  @Bean(initMethod = "init")
  public InsLabelServiceAdapter insLabelServiceAdapter(List<InsLabelAccessService> accessServices) {
    LOG.info("Discover instance label accessServices: [" + accessServices.size() + "]");
    InsLabelServiceAdapter insLabelServiceAdapter = new DefaultInsLabelServiceAdapter();
    accessServices.forEach(
        accessService -> {
          AdapterMode adapterMode =
              AnnotationUtils.findAnnotation(accessService.getClass(), AdapterMode.class);
          if (null != adapterMode) {
            LOG.info(
                "Register instance label access service: "
                    + accessService.getClass().getSimpleName()
                    + " to service adapter");
            insLabelServiceAdapter.registerServices(accessService, adapterMode.order());
          }
        });
    return insLabelServiceAdapter;
  }

  /** Configuration in eureka environment */
  /* @Configuration
  @ConditionalOnClass({EurekaClient.class})*/
  public static class EurekaClientConfiguration {
    @ConditionalOnMissingBean({EurekaInsLabelService.class})
    @Bean
    @Scope("prototype")
    public EurekaInsLabelService eurekaInsLabelService(EurekaDiscoveryClient discoveryClient) {
      return new EurekaInsLabelService(discoveryClient);
    }
  }

  /**
   * Enable the rpc service
   *
   * @return
   */
  /*//@ConditionalOnExpression("${wds.linkis.is.gateway:false}==false")
  @ConditionalOnMissingBean({InsLabelRpcService.class})
  public InsLabelRpcService insLabelRpcService(){
      LOG.info("Use the default implement of rpc service: [" + DefaultInsLabelRpcService.class + "]");
      return new DefaultInsLabelRpcService();
  }*/
}
