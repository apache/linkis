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

package org.apache.linkis.instance.label.service.impl;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.instance.label.entity.InstanceInfo;
import org.apache.linkis.instance.label.service.InsLabelAccessService;
import org.apache.linkis.instance.label.service.annotation.AdapterMode;
import org.apache.linkis.manager.label.entity.Label;

import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;

@AdapterMode
public class SpringInsLabelService implements InsLabelAccessService {

  public SpringInsLabelService(DiscoveryClient discoveryClient) {}

  @Override
  public void attachLabelToInstance(Label<?> label, ServiceInstance serviceInstance) {}

  @Override
  public void attachLabelsToInstance(
      List<? extends Label<?>> labels, ServiceInstance serviceInstance) {}

  @Override
  public void refreshLabelsToInstance(
      List<? extends Label<?>> labels, ServiceInstance serviceInstance) {}

  @Override
  public void removeLabelsFromInstance(ServiceInstance serviceInstance) {}

  @Override
  public List<ServiceInstance> searchInstancesByLabels(List<? extends Label<?>> labels) {
    return null;
  }

  @Override
  public List<ServiceInstance> searchInstancesByLabels(
      List<? extends Label<?>> labels, Label.ValueRelation relation) {
    return null;
  }

  @Override
  public List<ServiceInstance> searchUnRelateInstances(ServiceInstance serviceInstance) {
    return null;
  }

  @Override
  public List<ServiceInstance> searchLabelRelatedInstances(ServiceInstance serviceInstance) {
    return null;
  }

  @Override
  public void removeLabelsIfNotRelation(List<? extends Label<?>> labels) {}

  @Override
  public List<InstanceInfo> listAllInstanceWithLabel() {
    return null;
  }

  @Override
  public List<ServiceInstance> getInstancesByNames(String appName) {
    return null;
  }

  @Override
  public void removeInstance(ServiceInstance serviceInstance) {}

  @Override
  public void updateInstance(InstanceInfo instanceInfo) {}

  @Override
  public InstanceInfo getInstanceInfoByServiceInstance(ServiceInstance serviceInstance) {
    return null;
  }
}
