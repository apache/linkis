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
package com.webank.wedatasphere.linkis.cs.contextcache.test.keyword;

import com.webank.wedatasphere.linkis.cs.common.annotation.KeywordMethod;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;

/**
 * @author peacewong
 * @date 2020/2/13 16:32
 */
public class TestContextKey implements ContextKey {

    private  String key;

    private String keywords;

    @KeywordMethod
    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public ContextType getContextType() {
        return ContextType.METADATA;
    }

    @Override
    public void setContextType(ContextType contextType) {

    }

    @Override
    public ContextScope getContextScope() {
        return ContextScope.PUBLIC;
    }

    @Override
    public void setContextScope(ContextScope contextScope) {

    }

    @KeywordMethod(splitter = ",")
    @Override
    public String getKeywords() {
        return this.keywords;
    }

    @Override
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public void setType(int type) {

    }
}
