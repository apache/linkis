/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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
package com.webank.wedatasphere.linkis.cs.condition.impl;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.condition.AtomicCondition;
import com.webank.wedatasphere.linkis.cs.condition.ConditionType;

public class ContextValueTypeCondition extends AtomicCondition {

    Class contextValueType;

    public ContextValueTypeCondition(Class contextValueType) {
        this.contextValueType = contextValueType;
    }

    public Class getContextValueType() {
        return contextValueType;
    }

    public void setContextValueType(Class contextValueType) {
        this.contextValueType = contextValueType;
    }

    @Override
    public ConditionType getConditionType() {
        return ConditionType.Equals;
    }
}
