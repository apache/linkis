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

import org.apache.linkis.message.annotation.Implicit;
import org.apache.linkis.protocol.message.RequestProtocol;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
public class DefaultImplicitParser implements ImplicitParser {
    @Override
    public Map<String, List<ImplicitMethod>> parse(Object implicitObject) {
        Method[] methods = implicitObject.getClass().getMethods();
        return Arrays.stream(methods)
                .filter(this::methodFilterPredicate)
                .map(m -> this.getImplicitMethod(m, implicitObject))
                .collect(Collectors.groupingBy(ImplicitMethod::getOutput));
    }

    private ImplicitMethod getImplicitMethod(Method method, Object implicitObject) {
        ImplicitMethod implicitMethod = new ImplicitMethod();
        implicitMethod.setMethod(method);
        implicitMethod.setImplicitObject(implicitObject);
        implicitMethod.setInput(method.getParameterTypes()[0].getName());
        implicitMethod.setOutput(method.getReturnType().getName());
        return implicitMethod;
    }

    /**
     * 标注了@implicit注解
     * 入参数量只有一个，返回值不为void
     * 入参需要是RequestProtocol 的子类
     * 排除出参是入参的父类的情况
     *
     * @param method
     * @return
     */
    private boolean methodFilterPredicate(Method method) {
        if (method.getAnnotation(Implicit.class) != null
                && method.getParameterCount() == 1
                && !void.class.equals(method.getReturnType())) {
            // TODO: 2020/8/4 返回值支持集合 ，参数也可以不用是RequestProtocol的子类
            Class<?> input = method.getParameterTypes()[0];
            return RequestProtocol.class.isAssignableFrom(input) && !method.getReturnType().isAssignableFrom(input);
        }
        return false;
    }
}
