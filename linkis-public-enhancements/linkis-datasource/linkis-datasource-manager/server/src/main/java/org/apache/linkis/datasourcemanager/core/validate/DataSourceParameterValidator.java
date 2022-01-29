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
 
package org.apache.linkis.datasourcemanager.core.validate;

import org.apache.commons.lang.StringUtils;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import org.apache.linkis.datasourcemanager.core.validate.strategy.RegExpParameterValidateStrategy;
import org.apache.linkis.datasourcemanager.core.validate.strategy.TypeParameterValidateStrategy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DataSourceParameterValidator implements ParameterValidator {
    @PostConstruct
    public void initToRegister(){
        registerStrategy(new TypeParameterValidateStrategy());
        registerStrategy(new RegExpParameterValidateStrategy());
    }
    /**
     * strategies list
     */
    private List<ParameterValidateStrategy> strategies = new ArrayList<>();

    @Override
    public void registerStrategy(ParameterValidateStrategy strategy) {
        strategies.add(strategy);
    }

    @Override
    public void validate(List<DataSourceParamKeyDefinition> paramKeyDefinitions,
                         Map<String, Object> parameters) throws ParameterValidateException{
        //Covert parameters map to <DataSourceParamKeyDefinition.getId(), Object>
        Map<DataSourceParamKeyDefinition, Object> defToValue = paramKeyDefinitions.stream().filter(def ->
                Objects.nonNull(parameters.get(def.getKey())))
                .collect(Collectors.toMap(def -> def, def ->{
                    Object keyValue = parameters.get(def.getKey());
                    parameters.put(def.getKey(), keyValue);
                    return keyValue;
                }));
        for(DataSourceParamKeyDefinition def : paramKeyDefinitions){
            //Deal with cascade relation
            boolean needValidate = false;
            if(Objects.nonNull(def.getRefId())){
                DataSourceParamKeyDefinition refDef = new DataSourceParamKeyDefinition();
                refDef.setId(def.getRefId());
                Object refValue = defToValue.get(refDef);
                if(Objects.nonNull(refValue) && Objects.equals(refValue, def.getRefValue())){
                    needValidate = true;
                }
            }else{
                needValidate = true;
            }
            if (needValidate){
                String keyName = def.getKey();
                Object keyValue = parameters.get(def.getKey());
                DataSourceParamKeyDefinition.ValueType valueType = def.getValueType();
                if(null == keyValue ){
                    String defaultValue = def.getDefaultValue();
                    if(StringUtils.isNotBlank(defaultValue) &&
                            valueType == DataSourceParamKeyDefinition.ValueType.SELECT){
                        defaultValue = defaultValue.split(",")[0].trim();
                    }
                    keyValue = defaultValue;
                }
                if( null == keyValue || StringUtils.isBlank(String.valueOf(keyValue))){
                    if(def.isRequire()) {
                        throw new ParameterValidateException("Param Validate Failed[参数校验出错], [the value of key: '"
                                + keyName + " cannot be blank']");
                    }
                    continue;
                }
                for(ParameterValidateStrategy validateStrategy : strategies){
                    if(validateStrategy.accept(def.getValueType())) {
                        validateStrategy.validate(def, keyValue);
                    }
                }
            }
        }
    }

    @Override
    public List<ParameterValidateStrategy> getStrategies() {
        return strategies;
    }
}
