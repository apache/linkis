/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.instance.label.service;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.label.entity.Label;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;



@CacheConfig
public interface InsLabelService {
    /**
     * Add label to instance
     * @param label label entity
     * @param serviceInstance service instance
     */
    @CacheEvict(cacheNames = {"label"}, allEntries = true)
    void attachLabelToInstance(Label<?> label, ServiceInstance serviceInstance);

    @CacheEvict(cacheNames = {"label"}, allEntries = true)
    void attachLabelsToInstance(List<? extends Label<?>> labels, ServiceInstance serviceInstance);

    /**
     * Refresh all the labels of instance (to init the relationship of instance and labels)
     * @param labels
     * @param serviceInstance
     */
    @CacheEvict(cacheNames = {"instance"}, allEntries = true)
    void refreshLabelsToInstance(List<? extends Label<?>> labels, ServiceInstance serviceInstance);

    /**
     * Remove all relationship between label and instance
     * @param serviceInstance service instance
     */
    @CacheEvict(cacheNames = {"instance"}, allEntries = true)
    void removeLabelsFromInstance(ServiceInstance serviceInstance);

    /**
     * Search instances from labels
     * @param labels label list
     */
    @Cacheable({"instance"})
    List<ServiceInstance> searchInstancesByLabels(List<? extends Label<?>> labels);

    /**
     * Search instances that are not related with other labels
     * @param serviceInstance
     * @return
     */
    @Cacheable({"instance"})
    List<ServiceInstance> searchUnRelateInstances(ServiceInstance serviceInstance);

    /**
     * Search instances that are related with other labels
     * @param serviceInstance instance info for searching
     * @return
     */
    @Cacheable({"instance"})
    List<ServiceInstance> searchLabelRelatedInstances(ServiceInstance serviceInstance);

    void removeInstance(ServiceInstance serviceInstance);

    @CacheEvict(cacheNames = {"instance", "label"}, allEntries = true)
    void evictCache();
}
