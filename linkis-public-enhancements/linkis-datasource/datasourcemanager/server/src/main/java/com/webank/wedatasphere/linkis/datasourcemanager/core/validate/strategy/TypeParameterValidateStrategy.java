/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.datasourcemanager.core.validate.strategy;

import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import com.webank.wedatasphere.linkis.datasourcemanager.core.formdata.FormStreamContent;
import com.webank.wedatasphere.linkis.datasourcemanager.core.validate.ParameterValidateException;
import com.webank.wedatasphere.linkis.datasourcemanager.core.validate.ParameterValidateStrategy;
import com.webank.wedatasphere.linkis.metadatamanager.common.Json;

import java.util.List;
import java.util.Map;

import static com.webank.wedatasphere.linkis.datasourcemanager.core.formdata.CustomMultiPartFormDataTransformer.*;

/**
 * Type validate strategy
 * @author georgeqiao
 * 2020/02/13
 */
public class TypeParameterValidateStrategy implements ParameterValidateStrategy {
    @Override
    public boolean accept(DataSourceParamKeyDefinition.ValueType valueType) {
        //Accept all value
        return true;
    }

    @Override
    public Object validate(DataSourceParamKeyDefinition keyDefinition,
                         Object actualValue) throws ParameterValidateException {
        DataSourceParamKeyDefinition.ValueType valueType = keyDefinition.getValueType();
        Class<?> javaType = valueType.getJavaType();
        if(valueType == DataSourceParamKeyDefinition.ValueType.FILE ){
            if(!actualValue.getClass().equals(FormStreamContent.class)){
                throw new ParameterValidateException("Param Validate Failed[参数校验出错], [the value of '"
                        + keyDefinition.getKey() + "' must be 'File']");
            }
            return actualValue;
        }
        if(!javaType.isAssignableFrom(actualValue.getClass())){
            try {
                if(javaType.equals(List.class)){
                    return Json.fromJson(String.valueOf(actualValue), List.class, String.class);
                }else if(javaType.equals(Map.class)){
                    return Json.fromJson(String.valueOf(actualValue), Map.class, String.class, String.class);
                }else if(PrimitiveUtils.isPrimitive(javaType)){
                    return PrimitiveUtils.primitiveTypeConverse(actualValue, javaType);
                }
            }catch(Exception e){
                throw new ParameterValidateException("Param Validate Failed[参数校验出错], [type of value: '"
                    + actualValue + "' is not '" + javaType.getSimpleName() + "']");
            }
           throw new ParameterValidateException("Param Validate Failed[参数校验出错], [type of value: '"
                   + actualValue + "' is not '" + javaType.getSimpleName() + "']");
        }
        return actualValue;
    }
}
