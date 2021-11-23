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
import org.apache.linkis.message.parser.ImplicitMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class AbstractImplicitRegistry extends JavaLog implements ImplicitRegistry {

    private final AbstractMessageSchedulerContext context;
    /**
     * key: output
     */
    private final Map<String, List<ImplicitMethod>> implicitMethodCache = new ConcurrentHashMap<>();

    public AbstractImplicitRegistry(AbstractMessageSchedulerContext context) {
        this.context = context;
    }

    @SuppressWarnings("all")
    public final Interner<String> lock = Interners.<String>newWeakInterner();

    private final Map<String, Object> registedImplicitObjectMap = new ConcurrentHashMap<>();


    @Override
    @SuppressWarnings("all")
    public void register(Object implicitObject) {
        String implicitObjectName = implicitObject.getClass().getName();
        synchronized (this.lock.intern(implicitObjectName)) {
            //1.是否解析过
            Object o = this.registedImplicitObjectMap.get(implicitObjectName);
            if (o != null) return;
            Map<String, List<ImplicitMethod>> implicitMethods = this.context.getImplicitParser().parse(implicitObject);
            implicitMethods.forEach(this::refreshImplicitMethodCache);
            this.registedImplicitObjectMap.put(implicitObjectName, implicitObject);
        }
    }

    @SuppressWarnings("all")
    private void refreshImplicitMethodCache(String key, List<ImplicitMethod> implicitMethods) {
        synchronized (this.lock.intern(key)) {
            //同一个implicitObject 下的入参，出参相同的implit,会被过滤掉
            List<ImplicitMethod> implicitMethodsOld = this.implicitMethodCache.computeIfAbsent(key, k -> new ArrayList<>());
            for (ImplicitMethod implicitMethod : implicitMethods) {
                if (isImplicitRepeat(new ArrayList<>(implicitMethodsOld), implicitMethod)) {
                    // TODO: 2020/7/29 logging
                    continue;
                }
                implicitMethodsOld.add(implicitMethod);
            }
        }
    }

    private boolean isImplicitRepeat(List<ImplicitMethod> implicitMethodsOld, ImplicitMethod implicitMethod) {
        return implicitMethodsOld.stream().
                anyMatch(im -> im.getImplicitObject() == implicitMethod.getImplicitObject() && im.getInput().equals(implicitMethod.getInput()));
    }

    public Map<String, List<ImplicitMethod>> getImplicitMethodCache() {
        return this.implicitMethodCache;
    }
}
