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

package org.apache.linkis.cs.highavailable;

import org.apache.linkis.cs.common.entity.source.HAContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.highavailable.exception.CSErrorCode;
import org.apache.linkis.cs.highavailable.ha.BackupInstanceGenerator;
import org.apache.linkis.cs.highavailable.ha.ContextHAChecker;
import org.apache.linkis.cs.highavailable.ha.ContextHAIDGenerator;
import org.apache.linkis.cs.highavailable.proxy.MethodInterceptorImpl;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** ContextService高可用管理器默认实现 采用CGLib动态代理，一般用于CS持久层存储转换，将HAContextID实例进行转换 */
@Component
public class DefaultContextHAManager extends AbstractContextHAManager {

  private static final Logger logger = LoggerFactory.getLogger(DefaultContextHAManager.class);
  private static final Gson gson = new Gson();

  @Autowired private ContextHAIDGenerator contextHAIDGenerator;
  @Autowired private ContextHAChecker contextHAChecker;
  @Autowired private BackupInstanceGenerator backupInstanceGenerator;

  public DefaultContextHAManager() {}

  @Override
  public <T> T getContextHAProxy(T persistence) throws CSErrorException {
    Callback callback = new MethodInterceptorImpl(this, persistence);
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(persistence.getClass());
    Callback[] callbacks = new Callback[] {callback};
    enhancer.setCallbacks(callbacks);
    return (T) enhancer.create();
  }

  @Override
  public HAContextID convertProxyHAID(HAContextID oriHaContextID) throws CSErrorException {
    if (null == oriHaContextID) {
      logger.error("HaContextID cannot be null.");
      throw new CSErrorException(CSErrorCode.INVALID_HAID, "HaContextID cannot be null.");
    }
    HAContextID haContextID = oriHaContextID.copy();
    if (StringUtils.isBlank(haContextID.getContextId())) {
      // generate new haid
      HAContextID tmpHAID = contextHAIDGenerator.generateHAContextID(null);
      haContextID.setContextId(tmpHAID.getContextId());
      haContextID.setInstance(tmpHAID.getInstance());
      haContextID.setBackupInstance(tmpHAID.getBackupInstance());
      return haContextID;
    } else if (StringUtils.isNotBlank(haContextID.getInstance())
        && StringUtils.isNotBlank(haContextID.getBackupInstance())) {
      if (StringUtils.isNumeric(haContextID.getContextId())) {
        // convert contextID to haID
        String haIdKey = contextHAChecker.convertHAIDToHAKey(haContextID);
        haContextID.setContextId(haIdKey);
      } else if (contextHAChecker.isHAIDValid(haContextID.getContextId())) {
        String contextID =
            contextHAChecker.parseHAIDFromKey(haContextID.getContextId()).getContextId();
        haContextID.setContextId(contextID);
      } else {
        throw new CSErrorException(
            CSErrorCode.INVALID_HAID,
            "Invalid contextID in haContextID : " + gson.toJson(haContextID));
      }
      return haContextID;
    } else {
      // complete ha property
      if (StringUtils.isNumeric(haContextID.getContextId())) {
        HAContextID tmpHAID = contextHAIDGenerator.generateHAContextID(haContextID);
        haContextID.setInstance(tmpHAID.getInstance());
        haContextID.setBackupInstance(tmpHAID.getBackupInstance());
      } else if (contextHAChecker.isHAIDValid(haContextID.getContextId())) {
        HAContextID tmpHAID = contextHAChecker.parseHAIDFromKey(haContextID.getContextId());
        haContextID.setContextId(tmpHAID.getContextId());
        haContextID.setInstance(tmpHAID.getInstance());
        haContextID.setBackupInstance(tmpHAID.getBackupInstance());
      } else {
        throw new CSErrorException(
            CSErrorCode.INVALID_HAID,
            "Invalid contextID in haContextID : " + gson.toJson(haContextID));
      }
      // todo debug
      if (contextHAChecker.isHAContextIDValid(haContextID)) {
        logger.info("HAID : " + contextHAChecker.convertHAIDToHAKey(haContextID));
      }
      return haContextID;
    }
  }

  @Override
  public ContextHAIDGenerator getContextHAIDGenerator() {
    return contextHAIDGenerator;
  }

  @Override
  public ContextHAChecker getContextHAChecker() {
    return contextHAChecker;
  }

  @Override
  public BackupInstanceGenerator getBackupInstanceGenerator() {
    return backupInstanceGenerator;
  }
}
