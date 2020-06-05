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
package com.webank.wedatasphere.linkis.cs.listener.event.impl;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.listener.event.ContextKeyEvent;
import com.webank.wedatasphere.linkis.cs.listener.event.enumeration.OperateType;

/**
 * @author peacewong
 * @date 2020/2/15 21:30
 */
public class DefaultContextKeyEvent implements ContextKeyEvent {

    private ContextID contextID;

    private ContextKeyValue contextKeyValue;

    private ContextKeyValue oldValue;


    private OperateType operateType;

    public ContextID getContextID() {
        return contextID;
    }

    public void setContextID(ContextID contextID) {
        this.contextID = contextID;
    }

    public ContextKeyValue getContextKeyValue() {
        return contextKeyValue;
    }

    public void setContextKeyValue(ContextKeyValue contextKeyValue) {
        this.contextKeyValue = contextKeyValue;
    }

    public OperateType getOperateType() {
        return operateType;
    }

    public void setOperateType(OperateType operateType) {
        this.operateType = operateType;
    }

    public ContextKeyValue getOldValue() {
        return oldValue;
    }

    public void setOldValue(ContextKeyValue oldValue) {
        this.oldValue = oldValue;
    }

}
