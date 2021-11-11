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
 
package org.apache.linkis.manager.label.entity.entrance;

import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.entity.Feature;
import org.apache.linkis.manager.label.entity.GenericLabel;
import org.apache.linkis.manager.label.entity.annon.ValueSerialNum;

import java.util.HashMap;

public class LoadBalanceLabel extends GenericLabel implements JobStrategyLabel{

    public LoadBalanceLabel(){
        setLabelKey(LabelKeyConstant.LOAD_BALANCE_KEY);
    }

    @Override
    public Feature getFeature() {
        return Feature.OPTIONAL;
    }

    public Integer getCapacity() {
        if (null == getValue()) {
            return null;
        }
        return Integer.parseInt(getValue().get("capacity"));
    }

    @ValueSerialNum(0)
    public LoadBalanceLabel setCapacity(String capacity) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put("capacity", capacity);
        return this;
    }

    public String getGroupId() {
        if (null == getValue()) {
            return null;
        }
        return getValue().get("groupId");
    }

    @ValueSerialNum(1)
    public LoadBalanceLabel setGroupId(String groupId) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put("groupId", groupId);
        return this;
    }

    @Override
    public boolean equals(Object other) {
        if (LoadBalanceLabel.class.isInstance(other)) {
            if (null != getGroupId()) {
                return getGroupId().equals(((LoadBalanceLabel)other).getGroupId());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
