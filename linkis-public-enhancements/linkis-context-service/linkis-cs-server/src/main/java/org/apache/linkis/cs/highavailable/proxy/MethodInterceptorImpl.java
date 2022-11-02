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

package org.apache.linkis.cs.highavailable.proxy;

import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.HAContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.highavailable.AbstractContextHAManager;
import org.apache.linkis.cs.highavailable.exception.CSErrorCode;

import org.apache.commons.lang3.StringUtils;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于CGLib库实现的动态代理拦截器，拦截被代理方法的参数，在被代理方法之前和之后进行增强
 * 被代理方法调用前增强场景较多，一般对参数为HAContextID实例、参数名包含contextid的方法参数进行转换
 * 被代理方法调用后，目前只用于将HAContextID的数字型contextID转换为HAIDKey
 */
public class MethodInterceptorImpl implements MethodInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(MethodInterceptorImpl.class);
  private static final Gson gson = new Gson();
  private AbstractContextHAManager contextHAManager;
  private Object object;
  private final Map<Integer, String> contextIDCacheMap = new HashMap<Integer, String>();

  private static final String CONTEXTID = "contextid";
  private static final String GETCONTEXTID = "getcontextid";

  public MethodInterceptorImpl(AbstractContextHAManager contextHAManager, Object object) {
    this.contextHAManager = contextHAManager;
    this.object = object;
  }

  @Override
  public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy)
      throws Throwable {

    this.contextIDCacheMap.clear();
    // 1，执行方法前转换
    for (int i = 0; i < args.length; i++) {

      // ①参数含有ContextID实例
      if (ContextID.class.isInstance(args[i])) {
        ContextID contextID = (ContextID) args[i];
        convertContextIDBeforeInvoke(contextID, i);
      }

      // ②参数含有getContextID方法的
      convertGetContextIDBeforeInvoke(args[i]);
    }

    // ③方法名含有ContextID，并且有String类型参数，取第一个转换
    if (method.getName().toLowerCase().contains(CONTEXTID)) {
      for (int j = 0; j < args.length; j++) {
        if (String.class.isInstance(args[j])) {
          String pStr = (String) args[j];
          if (StringUtils.isNotBlank(pStr) && !StringUtils.isNumeric(pStr)) {
            if (this.contextHAManager.getContextHAChecker().isHAIDValid(pStr)) {
              String contextID =
                  this.contextHAManager.getContextHAChecker().parseHAIDFromKey(pStr).getContextId();
              args[j] = contextID;
            } else {
              logger.error("Invalid HAID : " + pStr + " in method : " + method.getName());
              throw new CSErrorException(
                  CSErrorCode.INVALID_HAID,
                  "Invalid HAID : " + pStr + " in method : " + method.getName());
            }
          }
          break;
        }
      }
    }

    // 2，执行原方法
    Object oriResult = method.invoke(this.object, args);

    // 3，执行方法后处理
    // （1）返回值处理
    if (null != oriResult) {
      // ①判断集合中元素是否有getContextID方法
      if (List.class.isInstance(oriResult)) {
        List objList = (List) oriResult;
        for (Object oneParameter : objList) {
          convertGetContextIDAfterInvoke(oneParameter);
        }
        // ②判断ContextID
      } else if (HAContextID.class.isInstance(oriResult)) {
        HAContextID haContextID = (HAContextID) oriResult;
        if (StringUtils.isNumeric(haContextID.getContextId())) {
          String haId = this.contextHAManager.getContextHAChecker().convertHAIDToHAKey(haContextID);
          haContextID.setContextId(haId);
        }
      } else {
        // ③ 返回值方法含有getContextID
        convertGetContextIDAfterInvoke(oriResult);
      }
    }

    // （2）请求参数还原
    // ①参数有ContextID实例
    for (int k = 0; k < args.length; k++) {
      if (ContextID.class.isInstance(args[k])) {
        if (this.contextIDCacheMap.containsKey(k)) {
          ContextID contextID = (ContextID) args[k];
          contextID.setContextId(this.contextIDCacheMap.get(k));
        } else {
          if (HAContextID.class.isInstance(args[k])) {
            HAContextID haContextID = (HAContextID) args[k];
            if (StringUtils.isNumeric(haContextID.getContextId())) {
              if (StringUtils.isNotBlank(haContextID.getInstance())
                  && StringUtils.isNotBlank(haContextID.getBackupInstance())) {
                String haId =
                    this.contextHAManager.getContextHAChecker().convertHAIDToHAKey(haContextID);
                haContextID.setContextId(haId);
              } else {
                logger.error("Invalid HAContextID : " + gson.toJson(haContextID));
                throw new CSErrorException(
                    CSErrorCode.INVAID_HA_CONTEXTID,
                    "Invalid HAContextID : " + gson.toJson(haContextID));
              }
            }
          }
        }
      } else {
        // ②参数含有getContextID方法
        convertGetContextIDAfterInvoke(args[k]);
      }
    }
    // ③方法名含有ContextID，并且有String类型参数 引用不需要作处理

    return oriResult;
  }

  private void convertContextIDBeforeInvoke(ContextID contextID, int index)
      throws CSErrorException {
    if (null == contextID) {
      return;
    }
    if (StringUtils.isNumeric(contextID.getContextId())) {
      if (HAContextID.class.isInstance(contextID)) {
        logger.error(
            "ContextId of HAContextID instance cannot be numberic. contextId : "
                + gson.toJson(contextID));
        throw new CSErrorException(
            CSErrorCode.INVALID_CONTEXTID,
            "ContextId of HAContextID instance cannot be numberic. contextId : "
                + gson.toJson(contextID));
      }
    } else {
      if (HAContextID.class.isInstance(contextID)) {
        if (null == contextID.getContextId()) {
          this.contextHAManager.convertProxyHAID((HAContextID) contextID);
        } else if (this.contextHAManager
            .getContextHAChecker()
            .isHAIDValid(contextID.getContextId())) {
          if (index > 0) {
            this.contextIDCacheMap.put(index, contextID.getContextId());
          }
          this.contextHAManager.convertProxyHAID((HAContextID) contextID);
        } else {
          logger.error("Invalid haContextId. contextId : " + gson.toJson(contextID));
          throw new CSErrorException(
              CSErrorCode.INVALID_HAID,
              "Invalid haContextId. contextId : " + gson.toJson(contextID));
        }
      }
    }
  }

  private void convertGetContextIDBeforeInvoke(Object object) throws CSErrorException {
    if (null == object) {
      return;
    }
    for (Method innerMethod : object.getClass().getMethods()) {
      if (innerMethod.getName().toLowerCase().contains(GETCONTEXTID)) {
        try {
          Object result = innerMethod.invoke(object);
          if (null != object && ContextID.class.isInstance(result)) {
            convertContextIDBeforeInvoke((ContextID) result, -1);
          } else {
            logger.warn(
                "Method {} returns non-contextid object : {}",
                innerMethod.getName(),
                gson.toJson(object));
          }
        } catch (Exception e) {
          logger.error("call method : {} error, ", innerMethod.getName(), e);
        }
      }
    }
  }

  private void convertGetContextIDAfterInvoke(Object object) throws CSErrorException {
    if (null == object) {
      return;
    }
    for (Method innerMethod : object.getClass().getMethods()) {
      convertGetContextIDAfterInvokeMethod(innerMethod, object);
    }
  }

  private void convertGetContextIDAfterInvokeMethod(Method method, Object methodObject)
      throws CSErrorException {
    if (method.getName().toLowerCase().contains(GETCONTEXTID)) {
      Object result = null;
      try {
        result = method.invoke(methodObject);
      } catch (Exception e) {
        logger.warn("Invoke method : {} error. ", method.getName(), e);
      }
      if (null != result && HAContextID.class.isInstance(result)) {
        HAContextID haContextID = (HAContextID) result;
        if (StringUtils.isNumeric(haContextID.getContextId())
            && StringUtils.isNotBlank(haContextID.getInstance())
            && StringUtils.isNotBlank(haContextID.getBackupInstance())) {
          String haid = this.contextHAManager.getContextHAChecker().convertHAIDToHAKey(haContextID);
          haContextID.setContextId(haid);
        } else {
          logger.error(
              "GetContextID method : "
                  + method.getName()
                  + " returns invalid haContextID : "
                  + gson.toJson(result));
          throw new CSErrorException(
              CSErrorCode.INVALID_HAID,
              "GetContextID method : "
                  + method.getName()
                  + " returns invalid haContextID : "
                  + gson.toJson(result));
        }
      }
    }
  }
}
