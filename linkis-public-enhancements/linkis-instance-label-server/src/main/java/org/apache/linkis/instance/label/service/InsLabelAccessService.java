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

package org.apache.linkis.instance.label.service;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.instance.label.entity.InstanceInfo;
import org.apache.linkis.instance.label.exception.InstanceErrorException;
import org.apache.linkis.manager.label.entity.Label;

import java.util.List;

public interface InsLabelAccessService {

  /**
   * Add label to instance
   *
   * @param label label entity
   * @param serviceInstance service instance
   */
  void attachLabelToInstance(Label<?> label, ServiceInstance serviceInstance)
      throws InstanceErrorException;

  void attachLabelsToInstance(List<? extends Label<?>> labels, ServiceInstance serviceInstance)
      throws InstanceErrorException;

  /**
   * Refresh all the labels of instance (to init the relationship of instance and labels)
   *
   * @param labels
   * @param serviceInstance
   */
  void refreshLabelsToInstance(List<? extends Label<?>> labels, ServiceInstance serviceInstance)
      throws InstanceErrorException;
  /**
   * Remove all relationship between label and instance
   *
   * @param serviceInstance service instance
   */
  void removeLabelsFromInstance(ServiceInstance serviceInstance);

  /**
   * Search instances from labels
   *
   * @param labels label list
   */
  List<ServiceInstance> searchInstancesByLabels(List<? extends Label<?>> labels);

  /**
   * Search instances from labels
   *
   * @param labels label list
   * @param relation relation type
   */
  List<ServiceInstance> searchInstancesByLabels(
      List<? extends Label<?>> labels, Label.ValueRelation relation);

  /**
   * Search instances that are not related with other labels
   *
   * @param serviceInstance instance info for searching
   * @return
   */
  List<ServiceInstance> searchUnRelateInstances(ServiceInstance serviceInstance);

  /**
   * Search instances that are related with other labels
   *
   * @param serviceInstance instance info for searching
   * @return
   */
  List<ServiceInstance> searchLabelRelatedInstances(ServiceInstance serviceInstance);
  /**
   * Remove labels
   *
   * @param labels
   */
  void removeLabelsIfNotRelation(List<? extends Label<?>> labels);

  /**
   * Search instances that are related with other labels
   *
   * @return
   */
  List<InstanceInfo> listAllInstanceWithLabel();

  List<ServiceInstance> getInstancesByNames(String appName);

  void removeInstance(ServiceInstance serviceInstance);

  void updateInstance(InstanceInfo instanceInfo);

  InstanceInfo getInstanceInfoByServiceInstance(ServiceInstance serviceInstance);
}
