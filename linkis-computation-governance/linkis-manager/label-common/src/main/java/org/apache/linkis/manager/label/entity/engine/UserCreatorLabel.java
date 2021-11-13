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
 
package org.apache.linkis.manager.label.entity.engine;

import org.apache.linkis.manager.label.constant.LabelConstant;
import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.entity.*;
import org.apache.linkis.manager.label.entity.annon.ValueSerialNum;
import org.apache.linkis.manager.label.exception.LabelErrorException;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;


public class UserCreatorLabel extends GenericLabel implements EngineNodeLabel, UserModifiable {

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

    @Override
    public Boolean getModifiable() {
        return modifiable;
    }

    @Override
    public void valueCheck(String stringValue) throws LabelErrorException {
        if(!StringUtils.isEmpty(stringValue)){
            if(stringValue.split(SerializableLabel.VALUE_SEPARATOR).length != 2){
                throw new LabelErrorException(LabelConstant.LABEL_BUILDER_ERROR_CODE,
                        "标签usercreator的值设置错误，需要2个值，并且需要使用"+VALUE_SEPARATOR+"隔开");
            }
        }
    }
}
