/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.rpc.message.utils;

import org.apache.linkis.DataWorkCloudApplication;
import org.apache.linkis.rpc.message.method.MethodExecuteWrapper;
import org.apache.linkis.rpc.message.parser.ServiceMethod;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  /**
   * find the MethodExecuteWrappers have the min order number in the MethodExecuteWrapper list
   *
   * @param methodExecuteWrappers the MethodExecuteWrapper list
   * @return the min MethodExecuteWrapper list
   */
  public static List<MethodExecuteWrapper> getMinOrders(
      List<MethodExecuteWrapper> methodExecuteWrappers) {
    if (methodExecuteWrappers == null || methodExecuteWrappers.isEmpty()) {
      return Collections.emptyList();
    }

    MethodExecuteWrapper minOrderMethodExecute = methodExecuteWrappers.get(0);

    for (MethodExecuteWrapper tmp : methodExecuteWrappers) {
      if (tmp.getOrder() < minOrderMethodExecute.getOrder()) {
        minOrderMethodExecute = tmp;
      }
    }

    List<MethodExecuteWrapper> result = new ArrayList<>();
    for (MethodExecuteWrapper tmp : methodExecuteWrappers) {
      if (tmp.getOrder() == minOrderMethodExecute.getOrder()) {
        result.add(tmp);
      }
    }
    return result;
  }

  /**
   * if the order number is the last/max number in the serviceMethod
   *
   * @param order the order number
   * @param serviceMethods the service methods
   * @return if the input order is the last/max order number
   */
  public static boolean orderIsLast(int order, List<ServiceMethod> serviceMethods) {
    if (order == Integer.MAX_VALUE) {
      return true;
    }
    for (ServiceMethod serviceMethod : serviceMethods) {
      if (serviceMethod.getOrder() > order) {
        return false;
      }
    }
    return true;
  }

  /**
   * find the first repeated order number, if there have no repeated order number return null
   *
   * @param serviceMethods serviceMethods
   * @return the repeated order number
   */
  public static Integer repeatOrder(List<ServiceMethod> serviceMethods) {
    Set<Integer> tmp = new HashSet<>();
    for (ServiceMethod serviceMethod : serviceMethods) {
      int order = serviceMethod.getOrder();
      if (tmp.contains(order)) {
        return order;
      } else {
        tmp.add(order);
      }
    }
    return null;
  }
}
