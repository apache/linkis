/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.message.registry;


import com.webank.wedatasphere.linkis.common.utils.JFunction0;
import com.webank.wedatasphere.linkis.common.utils.Utils;
import com.webank.wedatasphere.linkis.message.annotation.Implicit;
import com.webank.wedatasphere.linkis.message.context.AbstractMessageSchedulerContext;
import com.webank.wedatasphere.linkis.message.utils.MessageUtils;
import scala.runtime.AbstractFunction0;
import scala.runtime.AbstractFunction1;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

import static com.webank.wedatasphere.linkis.message.conf.MessageSchedulerConf.REFLECTIONS;

/**
 * @date 2020/7/28
 */
public class SpringImplicitRegistry extends AbstractImplicitRegistry {

    public SpringImplicitRegistry(AbstractMessageSchedulerContext context) {
        super(context);
        Set<Method> implicitMethods = REFLECTIONS.getMethodsAnnotatedWith(Implicit.class);
        Set<? extends Class<?>> implicitClasses = implicitMethods.stream().map(Method::getDeclaringClass).collect(Collectors.toSet());
        //区分出 bean中的方法，和其他，其他使用反射创建方法对象
        for (Class<?> implicitClass : implicitClasses) {
            Object bean = MessageUtils.getBean(implicitClass);
            if (bean == null) {
                bean = Utils.tryCatch(Utils.JFunction0(implicitClass::newInstance), new AbstractFunction1<Throwable, Object>() {
                    @Override
                    public Object apply(Throwable v1) {
                        logger().warn(String.format("reflection failed to create object %s", implicitClass.getName()));
                        return null;
                    }
                });
            }
            if (bean != null) this.register(bean);
        }
    }
}
