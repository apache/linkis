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
 
package org.apache.linkis.cs.highavailable.ha.impl;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.highavailable.exception.CSErrorCode;
import org.apache.linkis.cs.highavailable.ha.BackupInstanceGenerator;
import org.apache.linkis.cs.highavailable.ha.ContextHAChecker;
import org.apache.linkis.cs.highavailable.ha.instancealias.InstanceAliasManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class BackupInstanceGeneratorImpl implements BackupInstanceGenerator {
    private final static Logger logger = LoggerFactory.getLogger(BackupInstanceGeneratorImpl.class);

    @Autowired
    private InstanceAliasManager instanceAliasManager;

    @Autowired
    private ContextHAChecker contextHAChecker;

    @Override
    public String getBackupInstance(String haIDKey) throws CSErrorException {

        String alias = null;
        if (StringUtils.isNotBlank(haIDKey) && contextHAChecker.isHAIDValid(haIDKey)) {
            alias = contextHAChecker.parseHAIDFromKey(haIDKey).getBackupInstance();
        } else {
            throw new CSErrorException(CSErrorCode.INVALID_HAID, "Invalid HAID :" + haIDKey);
        }
        return alias;
    }

    @Override
    public String chooseBackupInstance(String mainInstanceAlias) throws CSErrorException {
        ServiceInstance mainInstance = null;
        try {
            mainInstance = instanceAliasManager.getInstanceByAlias(mainInstanceAlias);
        } catch (Exception e) {
            logger.error("Get Instance error, alias : {}, message : {}", mainInstanceAlias, e.getMessage());
            throw new CSErrorException(CSErrorCode.INVALID_INSTANCE_ALIAS, e.getMessage() + ", alias : " + mainInstanceAlias);
        }
        List<ServiceInstance> allInstanceList = instanceAliasManager.getAllInstanceList();
        List<ServiceInstance> remainInstanceList = new ArrayList<>();
        for (ServiceInstance instance : allInstanceList) {
            if (instance.equals(mainInstance)) {
                continue;
            }
            remainInstanceList.add(instance);
        }
        if (remainInstanceList.size() > 0) {
            int index = getBackupInstanceIndex(remainInstanceList);
            return instanceAliasManager.getAliasByServiceInstance(remainInstanceList.get(index));
        } else {
            // only one service instance
            logger.error("Only one instance, no remains.");
            return instanceAliasManager.getAliasByServiceInstance(mainInstance);
        }
    }

    private int getBackupInstanceIndex(List<ServiceInstance> instanceList) {

        // todo refactor according to load-balance
        return new Random().nextInt(instanceList.size());
    }

}
