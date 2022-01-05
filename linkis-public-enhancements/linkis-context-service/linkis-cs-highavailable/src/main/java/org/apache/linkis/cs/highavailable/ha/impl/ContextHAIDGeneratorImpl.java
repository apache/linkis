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

import com.google.gson.Gson;
import org.apache.linkis.DataWorkCloudApplication;
import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.cs.common.entity.source.CommonHAContextID;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.HAContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.highavailable.exception.CSErrorCode;
import org.apache.linkis.cs.highavailable.ha.BackupInstanceGenerator;
import org.apache.linkis.cs.highavailable.ha.ContextHAIDGenerator;
import org.apache.linkis.cs.highavailable.ha.instancealias.InstanceAliasConverter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContextHAIDGeneratorImpl implements ContextHAIDGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ContextHAIDGeneratorImpl.class);

    @Autowired
    private BackupInstanceGenerator backupInstanceGenerator;

    @Autowired
    private InstanceAliasConverter instanceAliasConverter;

    @Override
    public HAContextID generateHAContextID(ContextID contextID) throws CSErrorException {
        String contextIDKey = null;
        if (null != contextID && StringUtils.isNotBlank(contextID.getContextId())) {
            contextIDKey = contextID.getContextId();
        }

        ServiceInstance mainInstance = DataWorkCloudApplication.getServiceInstance();
        String mainInstanceAlias = instanceAliasConverter.instanceToAlias(mainInstance.getInstance());
        if (StringUtils.isBlank(mainInstanceAlias)) {
            logger.error("MainInstance cannot be null.");
            throw new CSErrorException(CSErrorCode.INVALID_INSTANCE, "MainInstance alias cannot be null.");
        }
        String backupInstance = backupInstanceGenerator.chooseBackupInstance(mainInstanceAlias);
        if (StringUtils.isBlank(backupInstance)) {
            logger.error("Generate backupInstance cannot be null.");
            throw new CSErrorException(CSErrorCode.GENERATE_BACKUP_INSTANCE_ERROR, "Generate backupInstance cannot be null.");
        }
        HAContextID haContextID = new CommonHAContextID(mainInstanceAlias, backupInstance, contextIDKey);
        logger.info("Generate a haContextID : {}" + new Gson().toJson(haContextID));
        return haContextID;
    }

}
