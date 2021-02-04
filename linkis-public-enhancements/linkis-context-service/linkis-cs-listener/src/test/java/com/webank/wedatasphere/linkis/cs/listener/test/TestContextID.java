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
package com.webank.wedatasphere.linkis.cs.listener.test;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;

/**
 * @author peacewong
 * @date 2020/2/13 20:41
 */
public class TestContextID implements ContextID {

    String contextID;

    @Override
    public String getContextId() {
        return contextID;
    }

    @Override
    public void setContextId(String contextId) {
        this.contextID = contextId;
    }
}
