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
 
package org.apache.linkis.message.parser;

import java.lang.reflect.Method;


public class ServiceMethod {

    private Object service;

    private Method method;

    private String alias;

    private String protocolName;

    private int order = 2147483647;

    private boolean allowImplicit = true;

    private boolean hasMethodContext;

    private ImplicitMethod implicitMethod;

    private boolean methodContextOnLeft;

    private String chainName = "default";

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    public ImplicitMethod getImplicitMethod() {
        return implicitMethod;
    }

    public void setImplicitMethod(ImplicitMethod implicitMethod) {
        this.implicitMethod = implicitMethod;
    }

    public boolean isHasMethodContext() {
        return hasMethodContext;
    }

    public void setHasMethodContext(boolean hasMethodContext) {
        this.hasMethodContext = hasMethodContext;
    }

    public boolean isAllowImplicit() {
        return allowImplicit;
    }

    public void setAllowImplicit(boolean allowImplicit) {
        this.allowImplicit = allowImplicit;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Object getService() {
        return service;
    }

    public void setService(Object service) {
        this.service = service;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public boolean isMethodContextOnLeft() {
        return methodContextOnLeft;
    }

    public void setMethodContextOnLeft(boolean methodContextOnLeft) {
        this.methodContextOnLeft = methodContextOnLeft;
    }
}
