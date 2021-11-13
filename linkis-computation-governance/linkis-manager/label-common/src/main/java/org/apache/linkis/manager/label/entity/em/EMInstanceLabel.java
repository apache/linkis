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
 
package org.apache.linkis.manager.label.entity.em;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.label.conf.LabelCommonConfig;
import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.entity.EMNodeLabel;
import org.apache.linkis.manager.label.entity.EngineNodeLabel;
import org.apache.linkis.manager.label.entity.GenericLabel;
import org.apache.linkis.manager.label.entity.annon.ValueSerialNum;
import org.apache.linkis.manager.label.entity.ResourceLabel;
import org.apache.linkis.manager.label.entity.node.NodeInstanceLabel;

import java.util.HashMap;

public class EMInstanceLabel extends GenericLabel implements NodeInstanceLabel, EMNodeLabel, EngineNodeLabel, ResourceLabel {


    public EMInstanceLabel() {
        setLabelKey(LabelKeyConstant.EM_INSTANCE_KEY);
    }


    public void setInstance(String instance) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put("instance", instance);
    }

    @ValueSerialNum(1)
    public void setServiceName(String serviceName) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put("serviceName", serviceName);
    }

    @ValueSerialNum(2)
    @Override
    public String getInstance(){
        if (null != getValue().get("instance")) {
            return getValue().get("instance");
        }
        return null;
    }

    @Override
    public String getServiceName(){
        if (null != getValue().get("serviceName")) {
            return getValue().get("serviceName");
        }
        return null;
    }

    public ServiceInstance getServiceInstance(){
        return ServiceInstance.apply(getServiceName(), getInstance());
    }

    @Override
    protected void setStringValue(String stringValue){
        String instance = stringValue.replaceFirst(LabelCommonConfig.ENGINE_CONN_MANAGER_SPRING_NAME.getValue() + "-", "");
        String serviceName = LabelCommonConfig.ENGINE_CONN_MANAGER_SPRING_NAME.getValue();
        setInstance(instance);
        setServiceName(serviceName);
    }

}
