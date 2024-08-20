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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Based on the dynamic proxy interceptor implemented by the CGLib library, it intercepts the
 * parameters of the proxy method and enhances it before and after the proxy method There are many
 * enhanced scenarios before being called by the proxy method. Generally, the method parameters
 * whose parameters are HAContextID instances and whose parameter names include contextid are
 * converted After being called by the proxy method, it is currently only used to convert the
 * numeric contextID of HAContextID to HAIDKey
 */
public class MethodInterceptorImpl implements MethodInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(MethodInterceptorImpl.class);
  private AbstractContextHAManager contextHAManager;
  private Object object;

  private static final String CONTEXTID = "contextid";
  private static final String GETCONTEXTID = "getcontextid";

  public MethodInterceptorImpl(AbstractContextHAManager contextHAManager, Object object) {
    this.contextHAManager = contextHAManager;
    this.object = object;
  }

  /**
   * 1. Judging the input parameter, if it is ContextID, convert it to a normal ID 2. Convert the
   * method whose name contains ContextID, and only judge the first parameter of String type 3.
   * Encapsulate the returned result as HA ID
   *
   * @param interceptObject
   * @param method
   * @param args
   * @param methodProxy
   * @return
   * @throws Throwable
   */
  @Override
  public Object intercept(
      Object interceptObject, Method method, Object[] args, MethodProxy methodProxy)
      throws Throwable {
    for (int i = 0; i < args.length; i++) {
      if (args[i] instanceof ContextID) {
        ContextID contextID = (ContextID) args[i];
        args[i] = convertContextIDBeforeInvoke(contextID);
      }
    }
    if (method.getName().toLowerCase().contains(CONTEXTID)) {
      for (int j = 0; j < args.length; j++) {
        if (args[j] instanceof String) {
          String contextIdStr = (String) args[j];
          if (StringUtils.isNotBlank(contextIdStr) && !StringUtils.isNumeric(contextIdStr)) {
            if (this.contextHAManager.getContextHAChecker().isHAIDValid(contextIdStr)) {
              String contextID =
                  this.contextHAManager
                      .getContextHAChecker()
                      .parseHAIDFromKey(contextIdStr)
                      .getContextId();
              args[j] = contextID;
            } else {
              logger.error("Invalid HAID : " + contextIdStr + " in method : " + method.getName());
              throw new CSErrorException(
                  CSErrorCode.INVALID_HAID,
                  "Invalid HAID : " + contextIdStr + " in method : " + method.getName());
            }
          }
          break;
        }
      }
    }

    Object oriResult = method.invoke(this.object, args);

    if (null != oriResult) {
      if (oriResult instanceof List) {
        List objList = (List) oriResult;
        for (Object oneParameter : objList) {
          if (null != objList && oneParameter instanceof HAContextID) {
            HAContextID haContextID = (HAContextID) oneParameter;
            if (StringUtils.isNumeric(haContextID.getContextId())) {
              String haId =
                  this.contextHAManager.getContextHAChecker().convertHAIDToHAKey(haContextID);
              haContextID.setContextId(haId);
            }
          }
        }
      } else if (oriResult instanceof HAContextID) {
        HAContextID haContextID = (HAContextID) oriResult;
        if (StringUtils.isNumeric(haContextID.getContextId())) {
          String haId = this.contextHAManager.getContextHAChecker().convertHAIDToHAKey(haContextID);
          haContextID.setContextId(haId);
        }
      }
    }
    return oriResult;
  }

  private ContextID convertContextIDBeforeInvoke(ContextID contextID) throws CSErrorException {
    if (null == contextID) {
      return null;
    }
    if (StringUtils.isNumeric(contextID.getContextId())) {
      if (contextID instanceof HAContextID) {
        logger.error(
            "ContextId of HAContextID instance cannot be numberic. contextId : {} ",
            contextID.getContextId());
        throw new CSErrorException(
            CSErrorCode.INVALID_CONTEXTID,
            "ContextId of HAContextID instance cannot be numberic. contextId : "
                + contextID.getContextId());
      }
    } else {
      if (contextID instanceof HAContextID) {
        if (null == contextID.getContextId()) {
          return this.contextHAManager.convertProxyHAID((HAContextID) contextID);
        } else if (this.contextHAManager
            .getContextHAChecker()
            .isHAIDValid(contextID.getContextId())) {
          return this.contextHAManager.convertProxyHAID((HAContextID) contextID);
        } else {
          logger.error("Invalid haContextId. contextId : {} ", contextID.getContextId());
          throw new CSErrorException(
              CSErrorCode.INVALID_HAID,
              "Invalid haContextId. contextId : " + contextID.getContextId());
        }
      }
    }
    return contextID;
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
      if (HAContextID.class.isInstance(result)) {
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
                  + result);
          throw new CSErrorException(
              CSErrorCode.INVALID_HAID,
              "GetContextID method : "
                  + method.getName()
                  + " returns invalid haContextID : "
                  + result);
        }
      }
    }
  }
}
