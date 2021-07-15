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

package com.webank.wedatasphere.linkis.manager.label.entity.em;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant;
import com.webank.wedatasphere.linkis.manager.label.entity.EMNodeLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.EngineNodeLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.GenericLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.annon.ValueSerialNum;
import com.webank.wedatasphere.linkis.manager.label.entity.ResourceLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.node.NodeInstanceLabel;

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
        String instance = stringValue.substring(stringValue.lastIndexOf('-') + 1, stringValue.length());
        String serviceName = stringValue.substring(0, stringValue.lastIndexOf('-'));
        setInstance(instance);
        setServiceName(serviceName);
    }

}
