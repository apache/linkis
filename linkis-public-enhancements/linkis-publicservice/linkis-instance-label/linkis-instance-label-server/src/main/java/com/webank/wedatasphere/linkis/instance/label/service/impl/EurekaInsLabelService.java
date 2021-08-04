/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.instance.label.service.impl;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.instance.label.entity.InstanceInfo;
import com.webank.wedatasphere.linkis.instance.label.service.InsLabelAccessService;
import com.webank.wedatasphere.linkis.instance.label.service.annotation.AdapterMode;
import com.webank.wedatasphere.linkis.manager.label.entity.Label;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;

import java.util.List;


@AdapterMode
public class EurekaInsLabelService implements InsLabelAccessService {

    public EurekaInsLabelService(EurekaDiscoveryClient discoveryClient){

    }


    @Override
    public void attachLabelToInstance(Label<?> label, ServiceInstance serviceInstance) {

    }

    @Override
    public void attachLabelsToInstance(List<? extends Label<?>> labels, ServiceInstance serviceInstance) {

    }

    @Override
    public void refreshLabelsToInstance(List<? extends Label<?>> labels, ServiceInstance serviceInstance) {

    }

    @Override
    public void removeLabelsFromInstance(ServiceInstance serviceInstance) {

    }

    @Override
    public List<ServiceInstance> searchInstancesByLabels(List<? extends Label<?>> labels) {
        return null;
    }

    @Override
    public List<ServiceInstance> searchInstancesByLabels(List<? extends Label<?>> labels, Label.ValueRelation relation) {
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
    public void removeLabelsIfNotRelation(List<? extends Label<?>> labels) {

    }

    @Override
    public List<InstanceInfo> listAllInstanceWithLabel() {
        return null;
    }

    @Override
    public void removeInstance(ServiceInstance serviceInstance) {

    }

    @Override
    public void updateInstance(InstanceInfo instanceInfo) {

    }

    @Override
    public InstanceInfo getInstanceInfoByServiceInstance(ServiceInstance serviceInstance) {
        return null;
    }
}
