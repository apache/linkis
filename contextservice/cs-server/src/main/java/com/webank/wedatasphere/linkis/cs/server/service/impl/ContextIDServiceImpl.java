/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.cs.server.service.impl;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.persistence.ContextPersistenceManager;
import com.webank.wedatasphere.linkis.cs.persistence.persistence.ContextIDPersistence;
import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceType;
import com.webank.wedatasphere.linkis.cs.server.service.ContextIDService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by patinousward on 2020/2/21.
 */
@Component
public class ContextIDServiceImpl extends ContextIDService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ContextPersistenceManager persistenceManager;

    private ContextIDPersistence getPersistence() throws CSErrorException {
        return persistenceManager.getContextIDPersistence();
    }

    @Override
    public String getName() {
        return ServiceType.CONTEXT_ID.name();
    }


    @Override
    public String createContextID(ContextID contextID) throws CSErrorException {
        getPersistence().createContextID(contextID);
        logger.info(String.format("createContextID,csId:%s", contextID.getContextId()));
        return contextID.getContextId();
    }

    @Override
    public ContextID getContextID(String id) throws CSErrorException {
        logger.info(String.format("getContextID,csId:%s", id));
        return getPersistence().getContextID(id);
    }

    @Override
    public void updateContextID(ContextID contextID) throws CSErrorException {
        logger.info(String.format("getContextID,csId:%s", contextID.getContextId()));
        getPersistence().updateContextID(contextID);
    }

    @Override
    public void resetContextID(String id) throws CSErrorException {
        // TODO: 2020/2/23 reset 方法
    }

    @Override
    public void removeContextID(String id) throws CSErrorException {
        logger.info(String.format("removeContextID,csId:%s", id));
        getPersistence().deleteContextID(id);
    }
}
