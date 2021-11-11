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
 
package org.apache.linkis.message.registry;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.apache.linkis.common.utils.JavaLog;
import org.apache.linkis.message.context.AbstractMessageSchedulerContext;
import org.apache.linkis.message.exception.MessageWarnException;
import org.apache.linkis.message.parser.ServiceMethod;
import org.apache.linkis.message.parser.ServiceParser;
import org.springframework.aop.support.AopUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class AbstractServiceRegistry extends JavaLog implements ServiceRegistry {

    @SuppressWarnings("all")
    public final Interner<String> lock = Interners.<String>newWeakInterner();
    /**
     * key:requestprotocol or custom implicit object class name
     */
    private final Map<String, List<ServiceMethod>> serviceMethodCache = new ConcurrentHashMap<>();

    private final Map<String, Object> registedServieMap = new ConcurrentHashMap<>();

    private final AbstractMessageSchedulerContext context;

    public AbstractServiceRegistry(AbstractMessageSchedulerContext context) {
        this.context = context;
    }

    @SuppressWarnings("all")
    @Override
    public void register(Object service) {
        String serviceName = AopUtils.getTargetClass(service).getName();
        synchronized (this.lock.intern(serviceName)) {
            //1.是否注册过
            Object o = this.registedServieMap.get(serviceName);
            if (o != null) return;
            //2..解析
            ServiceParser serviceParser = this.context.getservieParser();
            Map<String, List<ServiceMethod>> serviceMethods = serviceParser.parse(service);
            //3.注册
            serviceMethods.forEach(this::register);
            this.registedServieMap.put(serviceName, service);
        }
    }

    /**
     * @param key
     * @param value
     * @throws MessageWarnException
     */
    @SuppressWarnings("all")
    private void register(String key, List<ServiceMethod> serviceMethods) {
        //防止相同key在不同service的并发注册
        synchronized (this.lock.intern(key)) {
            //1.添加cache
            refreshServiceMethodCache(key, serviceMethods);
        }
    }

    private void refreshServiceMethodCache(String key, List<ServiceMethod> serviceMethods) {
        this.serviceMethodCache.computeIfAbsent(key, k -> new ArrayList<>()).addAll(serviceMethods);
    }

    public Map<String, List<ServiceMethod>> getServiceMethodCache() {
        return this.serviceMethodCache;
    }

}
