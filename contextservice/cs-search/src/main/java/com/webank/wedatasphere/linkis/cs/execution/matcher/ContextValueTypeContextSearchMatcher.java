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
package com.webank.wedatasphere.linkis.cs.execution.matcher;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContextTypeCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContextValueTypeCondition;
import org.reflections.ReflectionUtils;

public class ContextValueTypeContextSearchMatcher extends AbstractContextSearchMatcher{

    Class contextValueType;

    public ContextValueTypeContextSearchMatcher(ContextValueTypeCondition condition) {
        super(condition);
        contextValueType = condition.getContextValueType();
    }

    @Override
    public Boolean match(ContextKeyValue contextKeyValue) {
        return contextValueType.isInstance(contextKeyValue.getContextValue().getValue());
    }
}
