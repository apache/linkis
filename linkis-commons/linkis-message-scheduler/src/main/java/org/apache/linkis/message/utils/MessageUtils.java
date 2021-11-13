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
 
package org.apache.linkis.message.utils;

import org.apache.linkis.DataWorkCloudApplication;
import org.apache.linkis.message.parser.ServiceMethod;
import org.apache.linkis.message.scheduler.MethodExecuteWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MessageUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageUtils.class);

    public static <T> T getBean(Class<T> tClass) {
        T t = null;
        ApplicationContext applicationContext = DataWorkCloudApplication.getApplicationContext();
        if (applicationContext != null) {
            try {
                t = applicationContext.getBean(tClass);
            } catch (NoSuchBeanDefinitionException e) {
                LOGGER.warn(String.format("can not get bean from spring ioc:%s", tClass.getName()));
            }
        }
        return t;
    }

    public static boolean isAssignableFrom(String supperClassName, String className) {
        try {
            return Class.forName(supperClassName).isAssignableFrom(Class.forName(className));
        } catch (ClassNotFoundException e) {
            LOGGER.error("class not found", e);
            return false;
        }
    }

    public static boolean orderIsMin(MethodExecuteWrapper methodExecuteWrapper, List<MethodExecuteWrapper> methodExecuteWrappers) {
        for (MethodExecuteWrapper tmp : methodExecuteWrappers) {
            if (tmp.getOrder() < methodExecuteWrapper.getOrder()) {
                return false;
            }
        }
        return true;
    }

    public static boolean orderIsLast(int order, List<ServiceMethod> serviceMethods) {
        // TODO: 2020/8/5 方法判断修改为重复的order 支持头部
        if (order == 2147483647) return true;
        for (ServiceMethod serviceMethod : serviceMethods) {
            if (serviceMethod.getOrder() > order) {
                return false;
            }
        }
        return false;
    }

    public static Integer repeatOrder(List<ServiceMethod> serviceMethods) {
        Map<Integer, Integer> tmp = new HashMap<>();
        for (ServiceMethod serviceMethod : serviceMethods) {
            int order = serviceMethod.getOrder();
            if (tmp.get(order) == null) {
                tmp.put(order, order);
            } else {
                return order;
            }
        }
        return null;
    }

}
