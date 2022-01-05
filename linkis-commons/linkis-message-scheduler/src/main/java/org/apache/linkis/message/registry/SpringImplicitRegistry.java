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


import org.apache.linkis.message.annotation.Implicit;
import org.apache.linkis.message.context.AbstractMessageSchedulerContext;
import org.apache.linkis.message.utils.MessageUtils;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.linkis.message.conf.MessageSchedulerConf.REFLECTIONS;


public class SpringImplicitRegistry extends AbstractImplicitRegistry {

    public SpringImplicitRegistry(AbstractMessageSchedulerContext context) {
        super(context);
        Set<Method> implicitMethods = REFLECTIONS.getMethodsAnnotatedWith(Implicit.class);
        Set<? extends Class<?>> implicitClasses = implicitMethods.stream().map(Method::getDeclaringClass).collect(Collectors.toSet());
        //区分出 bean中的方法，和其他，其他使用反射创建方法对象
        for (Class<?> implicitClass : implicitClasses) {
            Object bean = MessageUtils.getBean(implicitClass);
            if (bean == null) {
                try {
                    bean = implicitClass.newInstance();
                } catch (Throwable t) {
                    logger().warn(String.format("reflection failed to create object %s", implicitClass.getName()));
                }
            }
            if (bean != null) this.register(bean);
        }
    }
}
