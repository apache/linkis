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

package com.webank.wedatasphere.linkis.datasourcemanager.core.validate;

import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;

/**
 * Parameter validate strategy
 * @author georgeqiao
 * 2020/02/11
 */
public interface ParameterValidateStrategy {
    /**
     * If accept value type
     * @param valueType validate type
     * @return result
     */
    boolean accept(DataSourceParamKeyDefinition.ValueType valueType);

    /**
     * Validate actual value by key definition
     * @param keyDefinition key definition
     * @param actualValue actual value
     * @return new value
     */
    Object validate(DataSourceParamKeyDefinition keyDefinition,
                  Object actualValue) throws ParameterValidateException;
}
