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
 
package org.apache.linkis.cs.persistence;

import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextKey;


public class AContextKey implements ContextKey {
    private String key = "key";

    private String keywords = "keywords..";

    private ContextScope contextScope = ContextScope.FRIENDLY;

    private ContextType contextType = ContextType.ENV;

    private Integer order = 3;

    private Double d = 6.0;

    private Long a = 66L;

    public Double getD() {
        return d;
    }

    public void setD(Double d) {
        this.d = d;
    }

    public Long getA() {
        return a;
    }

    public void setA(Long a) {
        this.a = a;
    }

    private String version = "v00001";

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

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
        return this.contextType;
    }

    @Override
    public void setContextType(ContextType contextType) {
        this.contextType = contextType;
    }

    @Override
    public ContextScope getContextScope() {
        return this.contextScope;
    }

    @Override
    public void setContextScope(ContextScope contextScope) {
        this.contextScope = contextScope;
    }

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
