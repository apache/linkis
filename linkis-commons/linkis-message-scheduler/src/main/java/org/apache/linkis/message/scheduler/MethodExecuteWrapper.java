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
 
package org.apache.linkis.message.scheduler;

import org.apache.linkis.message.parser.ImplicitMethod;
import org.apache.linkis.message.parser.ServiceMethod;

import java.lang.reflect.Method;


public class MethodExecuteWrapper {

    public MethodExecuteWrapper(ServiceMethod serviceMethod) {
        this.serviceMethod = serviceMethod;
    }

    private final ServiceMethod serviceMethod;

    public boolean shouldSkip;

    public boolean isShouldSkip() {
        return shouldSkip;
    }

    public void setShouldSkip(boolean shouldSkip) {
        this.shouldSkip = shouldSkip;
    }

    public Method getMethod() {
        return this.serviceMethod.getMethod();
    }

    public Object getService() {
        return this.serviceMethod.getService();
    }


    public String getAlias() {
        return this.serviceMethod.getAlias();
    }

    public int getOrder() {
        return this.serviceMethod.getOrder();
    }

    public String getChainName() {
        return this.serviceMethod.getChainName();
    }

    public boolean isHasMethodContext() {
        return this.serviceMethod.isHasMethodContext();
    }

    public ImplicitMethod getImplicitMethod() {
        return this.serviceMethod.getImplicitMethod();
    }

    public boolean isMethodContextOnLeft() {
        return this.serviceMethod.isMethodContextOnLeft();
    }

}
