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
package com.webank.wedatasphere.linkis.cs.persistence.entity;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextValue;
import com.webank.wedatasphere.linkis.cs.persistence.annotation.Ignore;

/**
 * Created by patinousward on 2020/2/12.
 */
@Ignore
public class PersistenceContextKeyValue implements ContextKeyValue {

    private Integer id;

    private String contextId;

    private ContextKey contextKey;

    private ContextValue contextValue;

    private String props;

    public String getProps() {
        return props;
    }

    public void setProps(String props) {
        this.props = props;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public void setContextKey(ContextKey contextKey) {
        this.contextKey = contextKey;
    }

    @Override
    public ContextKey getContextKey() {
        return this.contextKey;
    }

    @Override
    public ContextValue getContextValue() {
        return this.contextValue;
    }

    @Override
    public void setContextValue(ContextValue contextValue) {
        this.contextValue = contextValue;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }
}
