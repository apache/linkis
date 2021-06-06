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

package com.webank.wedatasphere.linkis.message.utils;

import com.webank.wedatasphere.linkis.DataWorkCloudApplication;
import com.webank.wedatasphere.linkis.common.utils.Utils;
import com.webank.wedatasphere.linkis.message.parser.ServiceMethod;
import com.webank.wedatasphere.linkis.message.scheduler.MethodExecuteWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import scala.runtime.AbstractFunction0;
import scala.runtime.AbstractFunction1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @date 2020/7/28
 */
public class MessageUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageUtils.class);

    public static <T> T getBean(Class<T> tClass) {
        T t = null;
        ApplicationContext applicationContext = DataWorkCloudApplication.getApplicationContext();
        if (applicationContext != null) {
            t = Utils.tryCatch(new AbstractFunction0<T>() {
                @Override
                public T apply() {
                    return applicationContext.getBean(tClass);
                }
            }, new AbstractFunction1<Throwable, T>() {
                @Override
                public T apply(Throwable v1) {
                    LOGGER.warn(String.format("can not get bean from spring ioc:%s", tClass.getName()));
                    return null;
                }
            });
        }
        return t;
    }

    public static boolean isAssignableFrom(String supperClassName, String className) {
        return Utils.tryCatch(Utils.JFunction0(() -> Class.forName(supperClassName).isAssignableFrom(Class.forName(className))), new AbstractFunction1<Throwable, Boolean>() {
            @Override
            public Boolean apply(Throwable v1) {
                LOGGER.error("class not found", v1);
                return false;
            }
        });
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
