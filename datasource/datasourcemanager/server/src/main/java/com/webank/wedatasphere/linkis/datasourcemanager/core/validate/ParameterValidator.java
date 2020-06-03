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

import java.util.List;
import java.util.Map;

/**
 * @author georgeqiao
 * 2020/02/11
 */


public interface ParameterValidator {
    /**
     * Register validate strategy
     * @param strategy strategy
     */
    void registerStrategy(ParameterValidateStrategy strategy);

    /**
     * Validate parameter dictionary
     * @param paramKeyDefinitions definitions
     * @param parameters parameters
     */
    void validate(List<DataSourceParamKeyDefinition> paramKeyDefinitions,
                  Map<String, Object> parameters) throws ParameterValidateException;

    /**
     * Get all strategies
     * @return
     */
    List<ParameterValidateStrategy> getStrategies();
}
