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
import org.apache.linkis.manager.label.entity.SerializableLabel;
import org.apache.linkis.manager.label.entity.annon.ValueSerialNum;

import java.util.HashMap;


public class BindEngineLabel extends GenericLabel implements JobStrategyLabel{

   public BindEngineLabel() {
       setLabelKey(LabelKeyConstant.BIND_ENGINE_KEY);
   }

   @Override
    public Feature getFeature() {
       return Feature.OPTIONAL;
   }

   public String getJobGroupId() {
       if (null == getValue()) {
           return null;
       }
       return getValue().get("jobGroupId");
   }

   public boolean getIsJobGroupHead() {
       if (null == getValue()) {
           return false;
       }
       return Boolean.parseBoolean(getValue().get("isJobGroupHead"));
   }

   public boolean getIsJobGroupEnd() {
       if (null == getValue()) {
           return false;
       }
       return Boolean.parseBoolean(getValue().get("isJobGroupEnd"));
   }

    @Override
    public boolean equals(Object other) {
       if (other instanceof BindEngineLabel) {
           if (null != getJobGroupId()) {
               return getJobGroupId().equals(((BindEngineLabel)other).getJobGroupId());
           } else {
               return false;
           }
       } else {
           return false;
       }
    }

    @ValueSerialNum(0)
    public BindEngineLabel setJobGroupId(String jobGroupId) {
       if (null == getValue()) {
           setValue(new HashMap<>());
       }
       getValue().put("jobGroupId", jobGroupId);
       return this;
   }

   @ValueSerialNum(1)
    public BindEngineLabel setIsJobGroupHead(String isHead) {
       if (null == getValue()) {
           setValue(new HashMap<>());
       }
       getValue().put("isJobGroupHead", isHead);
       return this;
   }

   @ValueSerialNum(2)
    public BindEngineLabel setIsJobGroupEnd(String isEnd) {
       if (null == getValue()) {
           setValue(new HashMap<>());
       }
       getValue().put("isJobGroupEnd", isEnd);
       return this;
   }

    @Override
    public String getStringValue() {

        return getJobGroupId() + SerializableLabel.VALUE_SEPARATOR + getIsJobGroupHead()+ SerializableLabel.VALUE_SEPARATOR  + getIsJobGroupEnd();
    }

}
