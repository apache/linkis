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

package com.webank.wedatasphere.linkis.manager.label.entity.engine;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant;
import com.webank.wedatasphere.linkis.manager.label.entity.EngineNodeLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.GenericLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.ResourceLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.annon.ValueSerialNum;
import com.webank.wedatasphere.linkis.manager.label.entity.node.NodeInstanceLabel;

import java.util.HashMap;

public class EngineInstanceLabel extends GenericLabel implements NodeInstanceLabel, EngineNodeLabel, ResourceLabel {

    public EngineInstanceLabel() {
        setLabelKey(LabelKeyConstant.ENGINE_INSTANCE_KEY);
    }

    @ValueSerialNum(1)
    public EngineInstanceLabel setInstance(String instance) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put("instance", instance);
        return this;
    }

    @ValueSerialNum(0)
    public EngineInstanceLabel setServiceName(String serviceName) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put("serviceName", serviceName);
        return this;
    }

    @Override
    public String getInstance() {
        if (null != getValue().get("instance")) {
            return getValue().get("instance");
        }
        return null;
    }

    @Override
    public String getServiceName() {
        if (null != getValue().get("serviceName")) {
            return getValue().get("serviceName");
        }
        return null;
    }

    public ServiceInstance getServiceInstance() {
        return ServiceInstance.apply(getServiceName(), getInstance());
    }
}
