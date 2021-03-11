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

import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant;
import com.webank.wedatasphere.linkis.manager.label.entity.EngineNodeLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.Feature;
import com.webank.wedatasphere.linkis.manager.label.entity.GenericLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.annon.ValueSerialNum;

import java.util.HashMap;

public class UserCreatorLabel extends GenericLabel implements EngineNodeLabel {

    public UserCreatorLabel() {
        setLabelKey(LabelKeyConstant.USER_CREATOR_TYPE_KEY);
    }

    @Override
    public Feature getFeature() {
        return Feature.CORE;
    }

    @ValueSerialNum(0)
    public void setUser(String user) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put("user", user);
    }

    @ValueSerialNum(1)
    public void setCreator(String creator) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put("creator", creator);
    }

    public String getUser() {
        if (null == getValue()) {
            return null;
        }
        return getValue().get("user");
    }

    public String getCreator() {

        if (null == getValue()) {
            return null;
        }
        return getValue().get("creator");
    }

}
