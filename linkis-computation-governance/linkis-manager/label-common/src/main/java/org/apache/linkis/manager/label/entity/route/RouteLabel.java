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
 
package org.apache.linkis.manager.label.entity.route;

import org.apache.linkis.manager.label.constant.LabelConstant;
import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.entity.InheritableLabel;
import org.apache.linkis.manager.label.entity.SerializableLabel;
import org.apache.linkis.manager.label.entity.UserModifiable;
import org.apache.linkis.manager.label.entity.annon.ValueSerialNum;
import org.apache.linkis.manager.label.exception.LabelErrorException;
import org.apache.commons.lang.StringUtils;


public class RouteLabel extends InheritableLabel<String> implements UserModifiable {


    public RouteLabel(){
        setLabelKey(LabelKeyConstant.ROUTE_KEY);
    }

    @ValueSerialNum(0)
    public void setRoutePath(String value){
        super.setValue(value);
    }

    @Override
    public Boolean getModifiable() {
        return modifiable;
    }

    @Override
    public void valueCheck(String stringValue) throws LabelErrorException {
        if(!StringUtils.isEmpty(stringValue)){
            if(stringValue.split(SerializableLabel.VALUE_SEPARATOR).length != 1){
                throw new LabelErrorException(LabelConstant.LABEL_BUILDER_ERROR_CODE,
                        "标签route的值设置错误，只能设置1个值，并且不能使用符号"+VALUE_SEPARATOR);
            }
        }
    }
    @Override
    public String getStringValue() {
        return getValue();
    }

    @Override
    protected void setStringValue(String stringValue) {
        setRoutePath(stringValue);
    }
}
